package com.njbank.ticket.service;

import com.njbank.ticket.dto.AIAnalysisResponse;
import com.njbank.ticket.dto.SimilarTicketDTO;
import com.njbank.ticket.dto.TemplateFieldDTO;
import com.njbank.ticket.entity.KnowledgeKeyword;
import com.njbank.ticket.entity.KnowledgeSolution;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl implements AIService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final SimilarityService similarityService;

    private volatile Map<String, Map<String, List<String>>> keywordCache = new HashMap<>();
    private volatile Map<String, String> solutionCache = new HashMap<>();
    private volatile Map<String, String> processingTimeCache = new HashMap<>();

    public AIServiceImpl(KnowledgeBaseService knowledgeBaseService, SimilarityService similarityService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.similarityService = similarityService;
    }

    @PostConstruct
    public void loadKnowledgeBase() {
        refreshCache();
    }

    public void refreshCache() {
        Map<String, Map<String, List<String>>> newKeywordCache = new HashMap<>();
        for (String category : Arrays.asList("TICKET_TYPE", "URGENCY", "DEPARTMENT", "CHANNEL", "SYSTEM")) {
            newKeywordCache.put(category, knowledgeBaseService.getKeywordMapByCategory(category));
        }

        Map<String, String> newSolutionCache = new HashMap<>();
        Map<String, String> newTimeCache = new HashMap<>();
        for (KnowledgeSolution sol : knowledgeBaseService.getAllSolutions()) {
            newSolutionCache.put(sol.getScenario(), sol.getSolution());
            newTimeCache.put(sol.getScenario(), sol.getProcessingTime());
        }

        keywordCache = newKeywordCache;
        solutionCache = newSolutionCache;
        processingTimeCache = newTimeCache;
    }

    @Override
    public AIAnalysisResponse analyzeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return buildDefaultResponse(title);
        }

        String lowerTitle = title.toLowerCase();
        String detectedType = detectByScore(keywordCache.getOrDefault("TICKET_TYPE", Collections.emptyMap()), lowerTitle, "投诉");
        String detectedUrgency = detectByScore(keywordCache.getOrDefault("URGENCY", Collections.emptyMap()), lowerTitle, "一般");
        String detectedDept = detectByScore(keywordCache.getOrDefault("DEPARTMENT", Collections.emptyMap()), lowerTitle, "零售业务部");
        String detectedChannel = detectByScore(keywordCache.getOrDefault("CHANNEL", Collections.emptyMap()), lowerTitle, "电话银行");
        String detectedSystem = detectByScore(keywordCache.getOrDefault("SYSTEM", Collections.emptyMap()), lowerTitle, "核心银行系统");

        String solutionKey = determineSolutionKey(lowerTitle, detectedType, detectedUrgency, detectedDept);
        String categoryPath = determineCategoryPath(lowerTitle, detectedType, detectedDept);
        String solution = generateDetailedSolution(lowerTitle, title, detectedType, detectedUrgency, detectedDept, detectedChannel, detectedSystem);
        String processingTime = determineProcessingTime(lowerTitle, detectedUrgency, detectedType);
        
        // 生成智能体对话风格的问题分析和建议操作
        String problemAnalysis = generateProblemAnalysis(lowerTitle, title, detectedType, detectedUrgency, detectedDept);
        String suggestedActions = generateSuggestedActions(lowerTitle, detectedType, detectedUrgency);

        double confidence = calculateConfidence(lowerTitle);

        // 查找相似工单
        List<SimilarTicketDTO> similarTickets = similarityService.findSimilarTickets(title, 3);

        // 生成问题模版字段
        List<TemplateFieldDTO> templateFields = generateTemplateFields(lowerTitle, detectedType, detectedDept);

        return AIAnalysisResponse.builder()
                .ticketType(detectedType)
                .ticketTypeDetail(detectedType + " - " + extractSubCategory(title, detectedType, detectedDept))
                .urgency(detectedUrgency)
                .department(detectedDept)
                .channel(detectedChannel)
                .relatedSystem(detectedSystem)
                .solution(solution)
                .processingTime(processingTime)
                .confidence(confidence)
                .title(title)
                .categoryPath(categoryPath)
                .problemAnalysis(problemAnalysis)
                .suggestedActions(suggestedActions)
                .similarTickets(similarTickets)
                .templateFields(templateFields)
                .build();
    }

    /**
     * 生成详细的具体推荐方案
     */
    private String generateDetailedSolution(String lowerTitle, String originalTitle, String type, String urgency, String dept, String channel, String system) {
        StringBuilder solution = new StringBuilder();

        // 根据具体问题类型生成针对性方案
        if (containsAny(lowerTitle, "转账", "汇款", "跨行", "同行", "转账失败", "汇款失败")) {
            solution.append("【转账汇款问题处理方案】\n\n");
            solution.append("1. 初步核查步骤：\n");
            solution.append("   • 核实客户账户余额及转账限额\n");
            solution.append("   • 确认收款方账户信息是否正确\n");
            solution.append("   • 查询交易流水确认资金去向\n");
            solution.append("   • 检查是否触发风控拦截\n\n");
            solution.append("2. 常见原因及处理：\n");
            solution.append("   • 超限额：引导客户提升转账限额或分笔转账\n");
            solution.append("   • 信息错误：协助核实收款方信息\n");
            solution.append("   • 风控拦截：转交风控部门核实处理\n");
            solution.append("   • 系统延迟：告知等待处理或重新发起\n\n");
            solution.append("3. 客户安抚话术：\n");
            solution.append("   \"请您放心，您的资金安全是我们的首要责任，");
            solution.append("我们会立即核查您的转账记录并尽快为您处理。\"\n");
        } else if (containsAny(lowerTitle, "登录", "登录不了", "登录失败", "密码错误", "忘记密码")) {
            solution.append("【登录异常处理方案】\n\n");
            solution.append("1. 身份验证：\n");
            solution.append("   • 核对客户身份信息（姓名+手机号+身份证）\n");
            solution.append("   • 验证近期交易密码\n\n");
            solution.append("2. 问题排查：\n");
            solution.append("   • 密码错误：引导使用\"忘记密码\"功能重置\n");
            solution.append("   • 账户锁定：核实原因后协助解锁\n");
            solution.append("   • 系统维护：告知预计恢复时间\n\n");
            solution.append("3. 建议操作：\n");
            solution.append("   • 清除浏览器缓存后重试\n");
            solution.append("   • 切换网络环境尝试\n");
            solution.append("   • 更新至最新版本APP\n\n");
            solution.append("4. 如仍无法解决：转交技术支持部门处理\n");
        } else if (containsAny(lowerTitle, "app", "手机银行", "闪退", "崩溃", "卡顿", "死机", "打不开")) {
            solution.append("【APP/手机银行故障处理方案】\n\n");
            solution.append("1. 故障类型判断：\n");
            solution.append("   • 闪退/崩溃：通常为APP版本或系统兼容问题\n");
            solution.append("   • 卡顿：网络或手机性能问题\n");
            solution.append("   • 打不开：应用损坏或系统限制\n\n");
            solution.append("2. 解决步骤：\n");
            solution.append("   • 步骤1：关闭后台应用，清理手机内存\n");
            solution.append("   • 步骤2：更新至应用商店最新版本\n");
            solution.append("   • 步骤3：卸载后重新安装APP\n");
            solution.append("   • 步骤4：如仍不行，尝试其他设备登录确认\n\n");
            solution.append("3. 替代方案：\n");
            solution.append("   • 可暂时使用网上银行或微信银行\n");
            solution.append("   • 拨打客服热线人工服务\n\n");
            solution.append("4. 记录反馈：收集手机型号、系统版本、APP版本，提交技术部门排查\n");
        } else if (containsAny(lowerTitle, "信用卡", "账单", "还款", "逾期", "分期", "积分", "额度")) {
            solution.append("【信用卡业务处理方案】\n\n");
            solution.append("1. 账单相关问题：\n");
            solution.append("   • 查询账单：告知账单日、还款日及金额\n");
            solution.append("   • 账单质疑：核实消费记录，必要时调单\n");
            solution.append("   • 分期办理：介绍手续费率及办理流程\n\n");
            solution.append("2. 还款问题：\n");
            solution.append("   • 还款方式：手机银行/ATM/网点/自动扣款\n");
            solution.append("   • 逾期处理：了解逾期原因，协助申请减免\n");
            solution.append("   • 宽限期：告知我行提供3天宽限期\n\n");
            solution.append("3. 额度问题：\n");
            solution.append("   • 临时额度：可申请临时调额\n");
            solution.append("   • 永久额度：满足用卡及还款条件后可申请\n\n");
            solution.append("4. 卡片问题：\n");
            solution.append("   • 卡片丢失：立即挂失，防止盗刷\n");
            solution.append("   • 卡片损坏：可申请换卡并邮寄\n");
        } else if (containsAny(lowerTitle, "贷款", "借款", "授信", "信用", "房贷", "车贷")) {
            solution.append("【贷款业务咨询方案】\n\n");
            solution.append("1. 贷款类型查询：\n");
            solution.append("   • 消费贷：纯信用，最高50万\n");
            solution.append("   • 经营贷：面向小微企业\n");
            solution.append("   • 房贷/车贷：抵押贷款，利率优惠\n\n");
            solution.append("2. 申请流程：\n");
            solution.append("   • 手机银行预申请 → 线下签约 → 审批放款\n");
            solution.append("   • 所需材料：身份证、收入证明、征信报告\n\n");
            solution.append("3. 常见问题：\n");
            solution.append("   • 审批时长：1-3个工作日\n");
            solution.append("   • 利率说明：根据征信情况定价\n");
            solution.append("   • 提前还款：满1年后可免手续费提前还款\n\n");
            solution.append("4. 如需进一步咨询：请转接贷款专员跟进\n");
        } else if (containsAny(lowerTitle, "理财", "基金", "收益", "净值", "亏损", "赎回")) {
            solution.append("【理财业务处理方案】\n\n");
            solution.append("1. 产品查询：\n");
            solution.append("   • 核实客户持有的理财产品/基金\n");
            solution.append("   • 查询当前净值及收益情况\n\n");
            solution.append("2. 收益问题：\n");
            solution.append("   • 市场波动属于正常现象\n");
            solution.append("   • 耐心解释产品风险等级\n");
            solution.append("   • 建议长期持有，平滑波动\n\n");
            solution.append("3. 赎回流程：\n");
            solution.append("   • 开放式产品：T+1到账\n");
            solution.append("   • 封闭式产品：到期赎回\n");
            solution.append("   • 赎回费率：根据持有时长计算\n\n");
            solution.append("4. 专业建议：建议转接理财顾问提供个性化方案\n");
        } else if (containsAny(lowerTitle, "冻结", "解冻", "封控", "封号", "限制")) {
            solution.append("【账户冻结/限制处理方案】\n\n");
            solution.append("1. 冻结原因排查：\n");
            solution.append("   • 风控拦截：异常交易触发自动保护\n");
            solution.append("   • 司法冻结：涉及法律案件配合调查\n");
            solution.append("   • 长期不动户：长期未使用被限制\n\n");
            solution.append("2. 解冻流程：\n");
            solution.append("   • 风控冻结：核实身份后即可解冻\n");
            solution.append("   • 司法冻结：需配合相关部门处理\n");
            solution.append("   • 不动户限制：携带证件到柜台激活\n\n");
            solution.append("3. 客户安抚：\n");
            solution.append("   \"账户安全保护机制是为了保障您的资金安全，");
            solution.append("我们会尽快核实并协助您处理。\"\n\n");
            solution.append("4. 注意事项：核实后需客户本人办理，不可代办\n");
        } else if (containsAny(lowerTitle, "密码", "pin", "口令", "验证码")) {
            solution.append("【密码/安全验证问题处理方案】\n\n");
            solution.append("1. 密码重置：\n");
            solution.append("   • 交易密码：可在线重置或到柜台办理\n");
            solution.append("   • 登录密码：通过手机号验证重置\n");
            solution.append("   • 动态密码：确认手机信号及运营商\n\n");
            solution.append("2. 安全验证失败：\n");
            solution.append("   • 验证码过期：重新获取（有效期5分钟）\n");
            solution.append("   • 次数超限：24小时后自动解锁\n");
            solution.append("   • 设备不匹配：尝试清除缓存或更换设备\n\n");
            solution.append("3. 紧急情况：\n");
            solution.append("   • 大额转账急需处理：到柜台办理\n");
            solution.append("   • 人脸识别失败：联系技术支持\n\n");
            solution.append("4. 安全提示：切勿向他人透露验证码和密码\n");
        } else if (containsAny(lowerTitle, "etc", "obu", "高速", "通行费", "etc故障")) {
            solution.append("【ETC业务处理方案】\n\n");
            solution.append("1. 设备问题：\n");
            solution.append("   • OBU设备故障：到银行网点检测/更换\n");
            solution.append("   • 标签脱落：重新激活设备\n");
            solution.append("   • 电量不足：更换设备电池或OBU\n\n");
            solution.append("2. 通行异常：\n");
            solution.append("   • 扣费异常：提供通行时间和入口出口核实\n");
            solution.append("   • 无法通行：改走人工通道，保留凭证\n");
            solution.append("   • 重复扣费：提交退款申请，5-7个工作日处理\n\n");
            solution.append("3. 办理业务：\n");
            solution.append("   • 绑定账户变更：到柜台或线上办理\n");
            solution.append("   • 注销ETC：需无欠款，携带身份证到柜台\n\n");
            solution.append("4. 客服热线：ECT客服电话 12345\n");
        } else if (containsAny(lowerTitle, "挂失", "补办", "补卡", "卡丢了", "卡坏了")) {
            solution.append("【挂失补办业务处理方案】\n\n");
            solution.append("1. 紧急挂失：\n");
            solution.append("   • 电话挂失：立即生效，保护资金安全\n");
            solution.append("   • 线上挂失：手机银行/网上银行操作\n");
            solution.append("   • 柜台挂失：携带身份证办理\n\n");
            solution.append("2. 补办流程：\n");
            solution.append("   • 挂失生效后7天可补办新卡\n");
            solution.append("   • 新卡卡号会变更，旧卡自动作废\n");
            solution.append("   • 补办周期：7-15个工作日\n\n");
            solution.append("3. 费用说明：\n");
            solution.append("   • 借记卡挂失：10元/次（部分卡种免费）\n");
            solution.append("   • 信用卡挂失：免手续费\n");
            solution.append("   • 补办工本费：根据卡种收取\n\n");
            solution.append("4. 注意事项：挂失后资金受保护，但需尽快补办\n");
        } else if (containsAny(lowerTitle, "商户", "pos", "收款", "扫码", "结算")) {
            solution.append("【商户收单业务处理方案】\n\n");
            solution.append("1. 交易问题：\n");
            solution.append("   • 交易未到账：核实账户信息及结算周期\n");
            solution.append("   • 交易失败：检查POS机具及网络状态\n");
            solution.append("   • 退款处理：联系发卡行发起退款\n\n");
            solution.append("2. 设备问题：\n");
            solution.append("   • POS机故障：联系收单机构维修/更换\n");
            solution.append("   • 网络异常：检查SIM卡或WIFI连接\n");
            solution.append("   • 打印故障：更换打印纸或联系维修\n\n");
            solution.append("3. 结算周期：\n");
            solution.append("   • T+1结算：次工作日到账\n");
            solution.append("   • D+0结算：当天到账（需申请）\n\n");
            solution.append("4. 费率问题：联系收单机构确认\n");
        } else if ("投诉".equals(type)) {
            solution.append("【客户投诉处理方案】\n\n");
            solution.append("1. 倾听记录：\n");
            solution.append("   • 认真倾听客户诉求，不打断\n");
            solution.append("   • 记录关键信息和客户情绪\n");
            solution.append("   • 表达理解和歉意\n\n");
            solution.append("2. 问题分析：\n");
            solution.append("   • 核实相关业务记录\n");
            solution.append("   • 确认客户投诉的合理性\n");
            solution.append("   • 评估影响程度和客户期望\n\n");
            solution.append("3. 处理方案：\n");
            solution.append("   • 能解决的：立即给出解决方案\n");
            solution.append("   • 不能解决的：承诺跟进时限\n");
            solution.append("   • 涉及赔偿的：按权限上报处理\n\n");
            solution.append("4. 跟进反馈：投诉处理后主动回访确认\n");
        } else if ("咨询".equals(type)) {
            solution.append("【业务咨询回复方案】\n\n");
            solution.append("1. 快速确认：\n");
            solution.append("   • 确认客户咨询的具体业务\n");
            solution.append("   • 核实客户身份和账户信息\n\n");
            solution.append("2. 专业解答：\n");
            solution.append("   • 根据客户需求介绍相关产品/服务\n");
            solution.append("   • 解答客户关心的费率、期限等问题\n");
            solution.append("   • 如有优惠活动主动告知\n\n");
            solution.append("3. 引导办理：\n");
            solution.append("   • 线上可办的：指导线上操作\n");
            solution.append("   • 需线下的：告知就近网点信息\n\n");
            solution.append("4. 如需进一步服务：记录需求，后续跟进\n");
        } else {
            // 默认通用方案
            solution.append("【工单处理建议】\n\n");
            solution.append("1. 问题确认：\n");
            solution.append("   • 详细了解客户反映的问题\n");
            solution.append("   • 核实相关账户和交易信息\n");
            solution.append("   • 记录问题发生的具体时间\n\n");
            solution.append("2. 初步处理：\n");
            solution.append("   • 根据问题类型转派至对应部门\n");
            solution.append("   • 提供临时解决建议\n");
            solution.append("   • 告知预计处理时间\n\n");
            solution.append("3. 跟进机制：\n");
            solution.append("   • 设置SLA提醒，确保及时处理\n");
            solution.append("   • 处理完成后主动回访客户\n");
            solution.append("   • 收集客户满意度反馈\n\n");
            solution.append("4. 如需升级处理：请联系我行客服热线：95588\n");
        }

        return solution.toString();
    }

    /**
     * 生成智能体对话风格的问题分析（解释问题发生的原因）
     */
    private String generateProblemAnalysis(String lowerTitle, String originalTitle, String type, String urgency, String dept) {
        StringBuilder analysis = new StringBuilder();
        
        if (containsAny(lowerTitle, "转账", "汇款", "跨行", "同行", "转账失败", "汇款失败")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("这通常涉及以下几个可能的原因：\n\n");
            analysis.append("1. 账户余额不足或超过当日转账限额：");
            analysis.append("系统会自动检测您的账户余额和转账限额，");
            analysis.append("如果超出限制，转账操作会被拦截。\n\n");
            analysis.append("2. 收款方信息填写错误：");
            analysis.append("账号、户名或开户行信息不匹配时，");
            analysis.append("交易无法成功完成。\n\n");
            analysis.append("3. 交易风控系统拦截：");
            analysis.append("为保护您的资金安全，我行风控系统会对异常交易进行实时监控，");
            analysis.append("如果检测到风险，会暂停交易。\n\n");
            analysis.append("4. 系统处理延迟：");
            analysis.append("跨行转账通常需要一定的处理时间，");
            analysis.append("请耐心等待，资金一般会在T+1工作日到账。");
        } else if (containsAny(lowerTitle, "登录", "登录不了", "登录失败", "密码错误", "忘记密码")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("登录失败可能由以下原因导致：\n\n");
            analysis.append("1. 密码输入错误：");
            analysis.append("连续输错密码会触发账户锁定机制，");
            analysis.append("请确认密码是否正确。\n\n");
            analysis.append("2. 账户被锁定：");
            analysis.append("为保障账户安全，连续输错密码5次后，");
            analysis.append("账户会被临时锁定24小时。\n\n");
            analysis.append("3. 网络环境异常：");
            analysis.append("当前网络不稳定或IP地址发生变化，");
            analysis.append("系统会认为存在安全风险。\n\n");
            analysis.append("4. 系统维护或升级：");
            analysis.append("我行可能正在进行系统维护，");
            analysis.append("期间部分功能可能暂时不可用。");
        } else if (containsAny(lowerTitle, "app", "手机银行", "闪退", "崩溃", "卡顿", "死机", "打不开")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("手机银行APP出现故障通常有以下原因：\n\n");
            analysis.append("1. APP版本过旧：");
            analysis.append("旧版本可能存在兼容性问题或已知BUG，");
            analysis.append("需要更新到最新版本。\n\n");
            analysis.append("2. 手机内存不足：");
            analysis.append("后台运行程序过多，");
            analysis.append("导致手机银行无法正常运行。\n\n");
            analysis.append("3. 系统兼容性问题：");
            analysis.append("手机操作系统版本过低或过高，");
            analysis.append("与APP存在兼容性冲突。\n\n");
            analysis.append("4. APP数据损坏：");
            analysis.append("长时间使用后可能出现数据损坏，");
            analysis.append("需要卸载后重新安装。");
        } else if (containsAny(lowerTitle, "信用卡", "账单", "还款", "逾期", "分期", "积分", "额度")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("这属于信用卡业务范畴，可能涉及以下情况：\n\n");
            analysis.append("1. 账单问题：");
            analysis.append("账单日通常为每月固定日期，");
            analysis.append("还款日为账单日后20天。\n\n");
            analysis.append("2. 还款失败：");
            analysis.append("还款账户余额不足、扣款时间过晚或系统处理延迟，");
            analysis.append("都可能导致还款失败。\n\n");
            analysis.append("3. 额度调整：");
            analysis.append("临时额度有使用期限，");
            analysis.append("到期后会自动恢复原有额度。\n\n");
            analysis.append("4. 逾期处理：");
            analysis.append("逾期会影响个人征信记录，");
            analysis.append("建议尽快还清欠款并联系客服说明情况。");
        } else if (containsAny(lowerTitle, "贷款", "借款", "授信", "信用", "房贷", "车贷")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("贷款业务相关问题可能涉及以下方面：\n\n");
            analysis.append("1. 申请条件：");
            analysis.append("贷款申请需要满足一定的信用评分、收入证明等条件，");
            analysis.append("不符合条件会导致申请被拒。\n\n");
            analysis.append("2. 审批流程：");
            analysis.append("贷款审批通常需要1-3个工作日，");
            analysis.append("期间会进行征信查询和资质审核。\n\n");
            analysis.append("3. 利率计算：");
            analysis.append("贷款利率根据贷款类型、期限和个人信用情况综合确定，");
            analysis.append("不同客户可能享受不同利率。\n\n");
            analysis.append("4. 还款方式：");
            analysis.append("支持等额本息、等额本金等多种还款方式，");
            analysis.append("您可以根据自身情况选择。");
        } else if (containsAny(lowerTitle, "理财", "基金", "收益", "净值", "亏损", "赎回")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("理财产品相关问题通常涉及：\n\n");
            analysis.append("1. 市场波动：");
            analysis.append("理财产品和基金的收益会随市场行情波动，");
            analysis.append("短期内出现净值下跌是正常现象。\n\n");
            analysis.append("2. 产品期限：");
            analysis.append("部分理财产品有封闭期，");
            analysis.append("期间无法提前赎回。\n\n");
            analysis.append("3. 风险等级：");
            analysis.append("不同产品有不同的风险等级，");
            analysis.append("高风险产品可能带来较高收益，也可能产生亏损。\n\n");
            analysis.append("4. 赎回规则：");
            analysis.append("开放式产品通常T+1到账，");
            analysis.append("大额赎回可能需要提前预约。");
        } else if (containsAny(lowerTitle, "冻结", "解冻", "封控", "封号", "限制")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("账户被冻结或限制通常由以下原因引起：\n\n");
            analysis.append("1. 风控系统触发：");
            analysis.append("检测到异常交易行为，如异地登录、大额转账等，");
            analysis.append("系统会自动冻结账户以保护资金安全。\n\n");
            analysis.append("2. 司法机关冻结：");
            analysis.append("涉及法律案件或配合调查时，");
            analysis.append("司法机关可依法冻结账户。\n\n");
            analysis.append("3. 长期未使用：");
            analysis.append("账户长期无交易活动，");
            analysis.append("系统会将其标记为不动户并限制使用。\n\n");
            analysis.append("4. 身份信息过期：");
            analysis.append("身份证等身份信息过期未更新，");
            analysis.append("账户功能会被限制。");
        } else if (containsAny(lowerTitle, "密码", "pin", "口令", "验证码")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("密码或验证码相关问题可能涉及：\n\n");
            analysis.append("1. 密码遗忘：");
            analysis.append("可以通过绑定的手机号或邮箱找回密码，");
            analysis.append("也可以到柜台办理密码重置。\n\n");
            analysis.append("2. 验证码收不到：");
            analysis.append("可能是手机信号问题、运营商拦截或短信箱已满，");
            analysis.append("验证码有效期为5分钟。\n\n");
            analysis.append("3. 安全验证失败：");
            analysis.append("人脸识别失败可能是光线不足或角度问题，");
            analysis.append("建议在光线充足的环境下重试。\n\n");
            analysis.append("4. 设备绑定问题：");
            analysis.append("更换设备登录可能需要验证原有设备，");
            analysis.append("确保账户安全。");
        } else if (containsAny(lowerTitle, "etc", "obu", "高速", "通行费", "etc故障")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("ETC相关问题通常涉及：\n\n");
            analysis.append("1. OBU设备故障：");
            analysis.append("OBU设备损坏、电量不足或标签脱落，");
            analysis.append("都会导致ETC无法正常使用。\n\n");
            analysis.append("2. 扣费异常：");
            analysis.append("通行记录未及时上传或扣费系统延迟，");
            analysis.append("可能导致扣费失败或重复扣费。\n\n");
            analysis.append("3. 账户余额不足：");
            analysis.append("ETC绑定的账户余额不足，");
            analysis.append("会导致扣费失败，影响通行。\n\n");
            analysis.append("4. 黑名单限制：");
            analysis.append("欠费或违规使用可能导致ETC被列入黑名单，");
            analysis.append("需要到网点解除限制。");
        } else if (containsAny(lowerTitle, "挂失", "补办", "补卡", "卡丢了", "卡坏了")) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("卡片挂失或补办通常涉及以下情况：\n\n");
            analysis.append("1. 卡片丢失：");
            analysis.append("卡片丢失后应立即挂失，");
            analysis.append("防止他人捡到后盗刷。\n\n");
            analysis.append("2. 卡片损坏：");
            analysis.append("磁条或芯片损坏会导致卡片无法读取，");
            analysis.append("需要更换新卡。\n\n");
            analysis.append("3. 挂失时效：");
            analysis.append("电话挂失立即生效，");
            analysis.append("柜台挂失需要本人携带身份证办理。\n\n");
            analysis.append("4. 补办流程：");
            analysis.append("挂失后7天可申请补办新卡，");
            analysis.append("新卡卡号会变更，旧卡自动作废。");
        } else if ("投诉".equals(type)) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("我们非常理解您的不满和困扰。\n\n");
            analysis.append("经过初步分析，您反映的问题可能涉及以下方面：\n\n");
            analysis.append("1. 服务体验不佳：");
            analysis.append("您在使用我行服务过程中遇到了不愉快的经历，");
            analysis.append("我们对此深表歉意。\n\n");
            analysis.append("2. 业务处理不当：");
            analysis.append("相关业务可能存在处理不及时或处理结果不符合预期的情况。\n\n");
            analysis.append("3. 沟通不畅：");
            analysis.append("可能存在信息传递不清晰或沟通渠道不顺畅的问题。\n\n");
            analysis.append("我们会认真对待您的投诉，");
            analysis.append("尽快核实情况并给出满意的答复。");
        } else if ("咨询".equals(type)) {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("这是一个业务咨询类问题。\n\n");
            analysis.append("经过分析，您可能想了解以下方面的信息：\n\n");
            analysis.append("1. 业务办理流程：");
            analysis.append("相关业务的办理条件、所需材料和具体流程。\n\n");
            analysis.append("2. 费率和收费标准：");
            analysis.append("涉及的手续费、利率、期限等详细信息。\n\n");
            analysis.append("3. 操作指引：");
            analysis.append("如何在手机银行、网上银行或柜台办理相关业务。\n\n");
            analysis.append("我们会为您提供详细的解答，");
            analysis.append("如有需要，也可以引导您到相应的业务部门咨询。");
        } else {
            analysis.append("根据您描述的\"").append(originalTitle).append("\"问题，");
            analysis.append("我们进行了初步分析：\n\n");
            analysis.append("这属于\"").append(type).append("\"类型的工单，");
            analysis.append("预计由\"").append(dept).append("\"负责处理。\n\n");
            analysis.append("问题的具体原因需要进一步核实，");
            analysis.append("建议您详细描述问题发生的时间、经过和具体表现，");
            analysis.append("以便我们更好地为您解决问题。");
        }

        return analysis.toString();
    }

    /**
     * 生成建议操作（简短的行动建议）
     */
    private String generateSuggestedActions(String lowerTitle, String type, String urgency) {
        StringBuilder actions = new StringBuilder();
        
        if (containsAny(lowerTitle, "转账", "汇款")) {
            actions.append("建议操作：\n");
            actions.append("1. 核实账户余额和转账限额\n");
            actions.append("2. 确认收款方信息是否正确\n");
            actions.append("3. 查询交易流水确认资金状态\n");
            actions.append("4. 如仍有问题，联系客服协助处理");
        } else if (containsAny(lowerTitle, "登录", "密码")) {
            actions.append("建议操作：\n");
            actions.append("1. 使用\"忘记密码\"功能重置密码\n");
            actions.append("2. 检查网络连接是否正常\n");
            actions.append("3. 清除浏览器缓存后重试\n");
            actions.append("4. 账户锁定后等待24小时自动解锁");
        } else if (containsAny(lowerTitle, "app", "手机银行")) {
            actions.append("建议操作：\n");
            actions.append("1. 更新手机银行到最新版本\n");
            actions.append("2. 清理手机内存，关闭后台应用\n");
            actions.append("3. 卸载后重新安装APP\n");
            actions.append("4. 尝试使用其他设备登录");
        } else if (containsAny(lowerTitle, "信用卡")) {
            actions.append("建议操作：\n");
            actions.append("1. 通过手机银行查询账单详情\n");
            actions.append("2. 确认还款账户余额充足\n");
            actions.append("3. 如需分期或调额，在线申请办理\n");
            actions.append("4. 逾期问题及时联系信用卡中心");
        } else if (containsAny(lowerTitle, "贷款")) {
            actions.append("建议操作：\n");
            actions.append("1. 通过手机银行查看贷款申请进度\n");
            actions.append("2. 准备好所需的申请材料\n");
            actions.append("3. 如有疑问，联系贷款专员咨询\n");
            actions.append("4. 按时还款，避免逾期影响征信");
        } else if (containsAny(lowerTitle, "理财")) {
            actions.append("建议操作：\n");
            actions.append("1. 查看产品说明书了解风险等级\n");
            actions.append("2. 通过手机银行查询收益明细\n");
            actions.append("3. 考虑长期持有以平滑市场波动\n");
            actions.append("4. 如需专业建议，预约理财顾问");
        } else if (containsAny(lowerTitle, "冻结")) {
            actions.append("建议操作：\n");
            actions.append("1. 核实近期是否有异常交易\n");
            actions.append("2. 携带身份证到柜台办理解冻\n");
            actions.append("3. 配合风控部门完成身份验证\n");
            actions.append("4. 更新过期的身份信息");
        } else if (containsAny(lowerTitle, "挂失")) {
            actions.append("建议操作：\n");
            actions.append("1. 立即电话挂失保护资金安全\n");
            actions.append("2. 7天后到柜台申请补办新卡\n");
            actions.append("3. 更新与旧卡关联的自动扣款业务\n");
            actions.append("4. 收到新卡后及时激活");
        } else if ("投诉".equals(type)) {
            actions.append("建议操作：\n");
            actions.append("1. 详细描述问题发生的时间和经过\n");
            actions.append("2. 提供相关凭证和截图\n");
            actions.append("3. 说明您期望的解决方案\n");
            actions.append("4. 保持电话畅通，等待回访");
        } else if ("咨询".equals(type)) {
            actions.append("建议操作：\n");
            actions.append("1. 明确您想了解的具体业务\n");
            actions.append("2. 准备好相关账户信息\n");
            actions.append("3. 通过手机银行自助查询\n");
            actions.append("4. 如需办理，在线预约或到柜台");
        } else {
            actions.append("建议操作：\n");
            actions.append("1. 详细描述问题的具体表现\n");
            actions.append("2. 提供问题发生的时间和地点\n");
            actions.append("3. 准备好相关的凭证材料\n");
            actions.append("4. 保持联系畅通，等待处理结果");
        }

        return actions.toString();
    }

    /**
     * 根据问题类型确定处理时间
     */
    private String determineProcessingTime(String lowerTitle, String urgency, String type) {
        if (containsAny(lowerTitle, "转账", "汇款")) return "2小时内";
        if (containsAny(lowerTitle, "登录", "密码")) return "1小时内";
        if (containsAny(lowerTitle, "app", "手机银行", "闪退")) return "4小时内";
        if (containsAny(lowerTitle, "冻结", "解冻")) return "1小时内";
        if (containsAny(lowerTitle, "挂失")) return "立即处理";
        if (containsAny(lowerTitle, "信用卡", "逾期")) return "24小时内";
        if (containsAny(lowerTitle, "贷款", "理财")) return "1-3个工作日";
        if (containsAny(lowerTitle, "投诉")) return "24小时内";
        if (containsAny(lowerTitle, "etc")) return "3个工作日内";
        if ("紧急".equals(urgency)) return "1小时内";
        if ("一般".equals(urgency)) return "24小时内";
        return "3个工作日内";
    }

    /**
     * 根据工单类型生成问题模版字段
     */
    private List<TemplateFieldDTO> generateTemplateFields(String lowerTitle, String type, String dept) {
        List<TemplateFieldDTO> fields = new ArrayList<>();

        // 根据不同的业务场景生成不同的模版
        if (containsAny(lowerTitle, "转账", "汇款", "跨行")) {
            fields.add(TemplateFieldDTO.builder().fieldName("转账金额").placeholder("请输入转账金额").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("收款账户").placeholder("请输入收款账户").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("转账时间").placeholder("请输入转账时间").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("错误提示").placeholder("请描述具体的错误提示信息").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("交易流水号").placeholder("如有交易流水号请填写").required(false).build());
        } else if (containsAny(lowerTitle, "登录", "登录失败", "密码错误")) {
            fields.add(TemplateFieldDTO.builder().fieldName("登录渠道").placeholder("手机银行/网上银行/微信银行").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("登录账号").placeholder("请输入登录账号").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("错误提示").placeholder("请描述具体的错误提示").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("最近登录时间").placeholder("请输入最近一次成功登录的时间").required(false).build());
        } else if (containsAny(lowerTitle, "app", "手机银行", "闪退", "崩溃", "卡顿")) {
            fields.add(TemplateFieldDTO.builder().fieldName("手机型号").placeholder("如：iPhone 13、华为Mate50").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("系统版本").placeholder("如：iOS 15.0、Android 12").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("APP版本").placeholder("请输入APP版本号").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("问题复现步骤").placeholder("请详细描述问题出现的步骤").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("错误截图").placeholder("如有错误截图请上传").required(false).build());
        } else if (containsAny(lowerTitle, "信用卡", "账单", "还款", "分期")) {
            fields.add(TemplateFieldDTO.builder().fieldName("信用卡卡号后四位").placeholder("请输入信用卡卡号后四位").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("账单日期").placeholder("请输入账单日期").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("问题描述").placeholder("请详细描述信用卡相关问题").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("期望处理方式").placeholder("请说明期望的处理方式").required(false).build());
        } else if (containsAny(lowerTitle, "贷款", "借款", "授信")) {
            fields.add(TemplateFieldDTO.builder().fieldName("贷款类型").placeholder("如：个人消费贷、房贷、车贷").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("贷款金额").placeholder("请输入贷款金额").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("问题描述").placeholder("请详细描述贷款相关问题").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("联系方式").placeholder("请提供联系方式").required(true).build());
        } else if (containsAny(lowerTitle, "冻结", "解冻", "封控")) {
            fields.add(TemplateFieldDTO.builder().fieldName("冻结账户").placeholder("请输入被冻结的账户").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("冻结时间").placeholder("请输入账户冻结时间").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("冻结原因").placeholder("如已知冻结原因请说明").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("近期交易").placeholder("请描述近期大额交易情况").required(false).build());
        } else if (containsAny(lowerTitle, "理财", "基金", "收益")) {
            fields.add(TemplateFieldDTO.builder().fieldName("理财产品名称").placeholder("请输入理财产品名称").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("购买金额").placeholder("请输入购买金额").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("购买日期").placeholder("请输入购买日期").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("问题描述").placeholder("请详细描述理财相关问题").required(true).build());
        } else {
            // 默认模版
            fields.add(TemplateFieldDTO.builder().fieldName("问题发生时间").placeholder("请输入问题发生的时间").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("问题详细描述").placeholder("请详细描述遇到的问题").required(true).build());
            fields.add(TemplateFieldDTO.builder().fieldName("已尝试解决方法").placeholder("请描述已经尝试的解决方法").required(false).build());
            fields.add(TemplateFieldDTO.builder().fieldName("期望结果").placeholder("请说明期望的处理结果").required(false).build());
        }

        return fields;
    }

    private String detectByScore(Map<String, List<String>> keywords, String lowerTitle, String defaultValue) {
        int maxScore = 0;
        String detected = defaultValue;
        for (Map.Entry<String, List<String>> entry : keywords.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lowerTitle.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            if (score > maxScore) {
                maxScore = score;
                detected = entry.getKey();
            }
        }
        return detected;
    }

    private String determineSolutionKey(String lowerTitle, String type, String urgency, String dept) {
        if (containsAny(lowerTitle, "空白", "白屏", "页面", "打不开", "加载")) return "页面空白";
        if (containsAny(lowerTitle, "扫码", "二维码", "扫不了", "扫码失败")) return "扫码异常";
        if (containsAny(lowerTitle, "app", "手机银行", "闪退", "崩溃", "卡顿", "死机")) return "App故障";
        if (containsAny(lowerTitle, "登录", "登录不了", "登录失败", "密码错误")) return "登录异常";
        if (containsAny(lowerTitle, "逾期", "还款", "罚息", "催收")) return "信用卡逾期";
        if (containsAny(lowerTitle, "贷款", "借款", "授信")) return "贷款咨询";
        if (containsAny(lowerTitle, "忘记密码", "密码忘了", "密码重置")) return "密码重置";
        if (containsAny(lowerTitle, "冻结", "解冻", "封控", "封号")) return "账户冻结";
        if (containsAny(lowerTitle, "理财", "基金", "收益", "净值")) return "理财产品";
        if (containsAny(lowerTitle, "挂失", "补办", "补卡", "卡丢了")) return "挂失补办";
        if (containsAny(lowerTitle, "etc", "高速", "obu", "通行费")) return "ETC问题";
        if (containsAny(lowerTitle, "商户", "收款", "结算", "交易", "pos")) return "商户收款";
        if (containsAny(lowerTitle, "外汇", "购汇", "结汇", "外币", "美元", "港币")) return "外汇业务";
        if (containsAny(lowerTitle, "转账", "汇款", "汇款失败", "转账超时")) return "转账失败";
        if (containsAny(lowerTitle, "账户", "账号", "户头")) return "账户异常";
        if (containsAny(lowerTitle, "密码", "口令", "pin")) return "密码问题";
        if ("咨询".equals(type)) return "业务咨询";
        return "默认";
    }

    private String determineCategoryPath(String lowerTitle, String type, String dept) {
        if (containsAny(lowerTitle, "转账", "汇款", "跨行", "同行")) return "零售业务 → 转账汇款 → 交易处理";
        if (containsAny(lowerTitle, "贷款", "借款", "信用", "授信")) return "信贷业务 → 贷款咨询 → 产品咨询";
        if (containsAny(lowerTitle, "信用卡", "账单", "还款", "分期", "积分")) return "信用卡业务 → 账户管理 → 账单查询";
        if (containsAny(lowerTitle, "理财", "基金", "收益", "净值", "申购")) return "理财业务 → 产品咨询 → 基金理财";
        if (containsAny(lowerTitle, "外汇", "购汇", "结汇", "外币")) return "外汇业务 → 汇兑咨询 → 购结汇";
        if (containsAny(lowerTitle, "挂失", "补办", "补卡", "换卡")) return "卡片服务 → 挂失补办 → 业务流程";
        if (containsAny(lowerTitle, "etc", "高速", "obu")) return "增值业务 → ETC服务 → 设备售后";
        if (containsAny(lowerTitle, "商户", "pos", "收款", "扫码")) return "收单业务 → 商户服务 → 交易处理";
        if (containsAny(lowerTitle, "账户", "密码", "登录", "冻结", "解锁")) return "安全合规 → 账户管理 → 安全验证";
        if (containsAny(lowerTitle, "空白", "白屏", "页面", "打不开", "加载", "app", "手机银行")) return "科技支持 → 系统运维 → 渠道故障";
        if ("投诉".equals(type)) return "客户服务 → 投诉管理 → 工单处理";
        if ("咨询".equals(type)) return "客户服务 → 业务咨询 → 信息查询";
        if ("办理".equals(type)) return "客户服务 → 业务办理 → 流程指引";
        return "客户服务 → 综合业务 → 一般问题";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private double calculateConfidence(String lowerTitle) {
        int matchCount = 0;
        for (Map<String, List<String>> categoryMap : keywordCache.values()) {
            for (List<String> kws : categoryMap.values()) {
                for (String kw : kws) {
                    if (lowerTitle.contains(kw.toLowerCase())) matchCount++;
                }
            }
        }
        return Math.min(0.99, 0.50 + (matchCount * 0.02));
    }

    private String extractSubCategory(String title, String type, String dept) {
        String lowerTitle = title.toLowerCase();
        if (containsAny(lowerTitle, "空白", "白屏", "打不开")) return "页面显示异常";
        if (containsAny(lowerTitle, "扫码", "二维码")) return "扫码交易异常";
        if (containsAny(lowerTitle, "app", "闪退", "崩溃", "卡顿")) return "移动端故障";
        if (containsAny(lowerTitle, "登录", "登录不了")) return "登录认证异常";
        if (containsAny(lowerTitle, "转账", "汇款")) return "转账汇款异常";
        if (containsAny(lowerTitle, "贷款", "借款")) return "贷款业务咨询";
        if (containsAny(lowerTitle, "理财", "基金")) return "理财业务咨询";
        if (containsAny(lowerTitle, "信用卡", "账单")) return "信用卡账务咨询";
        if (containsAny(lowerTitle, "逾期", "还款", "罚息")) return "逾期还款咨询";
        if (containsAny(lowerTitle, "冻结", "解冻")) return "账户冻结处理";
        if (containsAny(lowerTitle, "挂失", "补办", "补卡")) return "卡片挂失补办";
        if (containsAny(lowerTitle, "etc", "obu")) return "ETC设备售后";
        if (containsAny(lowerTitle, "商户", "pos", "收款")) return "商户收单异常";
        if (containsAny(lowerTitle, "外汇", "购汇", "结汇")) return "外汇业务咨询";
        if (containsAny(lowerTitle, "密码", "pin")) return "密码安全验证";
        if (containsAny(lowerTitle, "账户", "账号")) return "账户管理咨询";
        if ("投诉".equals(type)) return "客户投诉处理";
        if ("咨询".equals(type)) return "业务信息咨询";
        if ("办理".equals(type)) return "业务办理指引";
        return "一般问题处理";
    }

    private AIAnalysisResponse buildDefaultResponse(String title) {
        List<TemplateFieldDTO> defaultFields = new ArrayList<>();
        defaultFields.add(TemplateFieldDTO.builder().fieldName("问题发生时间").placeholder("请输入问题发生的时间").required(true).build());
        defaultFields.add(TemplateFieldDTO.builder().fieldName("问题详细描述").placeholder("请详细描述遇到的问题").required(true).build());
        defaultFields.add(TemplateFieldDTO.builder().fieldName("已尝试解决方法").placeholder("请描述已经尝试的解决方法").required(false).build());
        defaultFields.add(TemplateFieldDTO.builder().fieldName("期望结果").placeholder("请说明期望的处理结果").required(false).build());

        return AIAnalysisResponse.builder()
                .ticketType("投诉")
                .ticketTypeDetail("投诉 - 一般问题")
                .urgency("一般")
                .department("零售业务部")
                .channel("电话银行")
                .relatedSystem("核心银行系统")
                .solution("1. 记录客户诉求\n2. 转派至相关业务部门\n3. 预计处理时效：24小时内")
                .processingTime("24小时内")
                .confidence(0.5)
                .title(title != null ? title : "")
                .categoryPath("零售业务 → 综合业务 → 一般问题")
                .similarTickets(Collections.emptyList())
                .templateFields(defaultFields)
                .build();
    }
}
