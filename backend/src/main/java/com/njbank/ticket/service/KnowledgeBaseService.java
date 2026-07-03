package com.njbank.ticket.service;

import com.njbank.ticket.entity.KnowledgeArticle;
import com.njbank.ticket.entity.KnowledgeKeyword;
import com.njbank.ticket.entity.KnowledgeSolution;
import com.njbank.ticket.repository.KnowledgeArticleRepository;
import com.njbank.ticket.repository.KnowledgeKeywordRepository;
import com.njbank.ticket.repository.KnowledgeSolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private final KnowledgeKeywordRepository keywordRepository;
    private final KnowledgeSolutionRepository solutionRepository;
    private final KnowledgeArticleRepository articleRepository;

    public KnowledgeBaseService(KnowledgeKeywordRepository keywordRepository,
                                KnowledgeSolutionRepository solutionRepository,
                                KnowledgeArticleRepository articleRepository) {
        this.keywordRepository = keywordRepository;
        this.solutionRepository = solutionRepository;
        this.articleRepository = articleRepository;
    }

    @PostConstruct
    public void initDefaultData() {
        if (keywordRepository.count() == 0) {
            initKeywords();
        }
        if (solutionRepository.count() == 0) {
            initSolutions();
        }
        if (articleRepository.count() == 0) {
            initArticles();
        }
    }

    private void initKeywords() {
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("TICKET_TYPE").keywordName("投诉")
                .keywords("投诉,不满,差评,反馈,申诉,举报,维权,退款,欺诈,被骗,态度,推诿,严重,批评")
                .sortOrder(1).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("TICKET_TYPE").keywordName("咨询")
                .keywords("咨询,询问,怎么办,怎么,如何,请问,想知道,查询,余额,利率,手续费,多少,几点,可以,能不能")
                .sortOrder(2).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("TICKET_TYPE").keywordName("办理")
                .keywords("办理,开户,开通,申请,激活,绑定,挂失,补办,修改,变更,转账,汇款,设置,签约,解约,注销")
                .sortOrder(3).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("TICKET_TYPE").keywordName("故障")
                .keywords("故障,不能用,无法,错误,崩溃,卡死,闪退,异常,超时,打不开,登录不了,交易失败,白屏,空白,黑屏,乱码,死机,卡顿,延迟,中断,停止,扫码,二维码")
                .sortOrder(4).build());

        keywordRepository.save(KnowledgeKeyword.builder()
                .category("URGENCY").keywordName("紧急")
                .keywords("紧急,急,马上,立即,资金,账户,密码,冻结,盗用,被盗,异常,欺诈,转账失败,扣款,丢失,诈骗,风险,危险")
                .sortOrder(1).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("URGENCY").keywordName("一般")
                .keywords("咨询,查询,了解,办理,申请,开通,进度,怎么,如何,请问,操作,设置")
                .sortOrder(2).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("URGENCY").keywordName("较低")
                .keywords("建议,优化,体验,希望,希望增加,希望改进,能不能,如果,以后")
                .sortOrder(3).build());

        keywordRepository.save(KnowledgeKeyword.builder()
                .category("DEPARTMENT").keywordName("零售业务部")
                .keywords("储蓄,存款,取款,转账,汇款,理财,手机银行,网上银行,电话银行,个人,信用卡,贷款,消费,借记卡,储蓄卡,银行卡,扫码,二维码,收银")
                .sortOrder(1).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("DEPARTMENT").keywordName("信用卡中心")
                .keywords("信用卡,卡号,额度,账单,还款,分期,年费,积分,刷卡,信用,贷记卡,预借,现金分期,消费分期")
                .sortOrder(2).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("DEPARTMENT").keywordName("对公业务部")
                .keywords("对公,企业,公司,开户,年检,变更,基本户,一般户,货款,票据,企业网银,对公账户,对公业务")
                .sortOrder(3).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("DEPARTMENT").keywordName("风控合规部")
                .keywords("风险,合规,反洗钱,可疑,大额,频繁,冻结,解冻,涉案,限额,管制,黑名单,白名单")
                .sortOrder(4).build());

        keywordRepository.save(KnowledgeKeyword.builder()
                .category("CHANNEL").keywordName("手机银行")
                .keywords("手机,app,移动端,掌上银行,android,ios,安卓,苹果")
                .sortOrder(1).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("CHANNEL").keywordName("网上银行")
                .keywords("网银,电脑,网页,网上,pc端,pc,u盾,ukey")
                .sortOrder(2).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("CHANNEL").keywordName("电话银行")
                .keywords("电话,客服,热线,人工,来电,手机拨打")
                .sortOrder(3).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("CHANNEL").keywordName("微信银行")
                .keywords("微信,公众号,小程序,wechat")
                .sortOrder(4).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("CHANNEL").keywordName("网点")
                .keywords("柜台,网点,营业厅,线下,现场,柜员")
                .sortOrder(5).build());

        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("核心银行系统")
                .keywords("转账,汇款,交易,账户,余额,扣款,到账,清算,结算,划转")
                .sortOrder(1).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("信用卡系统")
                .keywords("信用卡,账单,还款,分期,额度,积分,年费")
                .sortOrder(2).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("网上银行系统")
                .keywords("网银,u盾,数字证书,登录,安全,证书,ukey")
                .sortOrder(3).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("手机银行系统")
                .keywords("app,手机银行,移动端,推送,android,ios")
                .sortOrder(4).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("柜面系统")
                .keywords("柜台,网点,柜员,现金,存取,对私")
                .sortOrder(5).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("信贷系统")
                .keywords("贷款,借款,还款,逾期,授信,额度,信用")
                .sortOrder(6).build());
        keywordRepository.save(KnowledgeKeyword.builder()
                .category("SYSTEM").keywordName("微信银行系统")
                .keywords("微信,公众号,小程序,h5")
                .sortOrder(7).build());
    }

    private void initSolutions() {
        solutionRepository.save(KnowledgeSolution.builder().scenario("转账失败").category("TRANSFER")
                .solution("1. 查询核心系统交易状态\n2. 如已扣款未到账，发起资金调账\n3. 预计处理时效：2小时内").processingTime("2小时内").sortOrder(1).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("账户异常").category("ACCOUNT")
                .solution("1. 核实账户状态和交易明细\n2. 如有风险立即冻结账户\n3. 联系客户确认情况\n预计处理时效：1小时内").processingTime("1小时内").sortOrder(2).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("密码问题").category("ACCOUNT")
                .solution("1. 核实客户身份信息\n2. 重置密码或解锁\n3. 预计处理时效：30分钟内").processingTime("30分钟内").sortOrder(3).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("业务咨询").category("CONSULT")
                .solution("1. 详细解答客户咨询\n2. 提供相关业务办理指引\n3. 预计处理时效：实时").processingTime("实时").sortOrder(4).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("页面空白").category("TECH")
                .solution("1. 清除浏览器缓存后重试\n2. 更换浏览器或使用手机银行APP\n3. 如仍不行，怀疑核心系统接口异常，联系科技部排查\n预计处理时效：1小时内").processingTime("1小时内").sortOrder(5).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("扫码异常").category("TECH")
                .solution("1. 确认扫码设备与网络正常\n2. 核实二维码有效期及商户状态\n3. 检查核心系统交易路由配置\n预计处理时效：2小时内").processingTime("2小时内").sortOrder(6).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("App故障").category("TECH")
                .solution("1. 强制退出APP并重启\n2. 清除APP缓存或卸载重装\n3. 确认手机系统版本是否兼容\n预计处理时效：4小时内").processingTime("4小时内").sortOrder(7).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("登录异常").category("ACCOUNT")
                .solution("1. 核实账户状态是否正常\n2. 确认安全认证方式是否过期\n3. 检查是否为限制登录名单\n预计处理时效：30分钟内").processingTime("30分钟内").sortOrder(8).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("信用卡逾期").category("CREDIT")
                .solution("1. 核实逾期金额与天数\n2. 引导客户尽快还款\n3. 如有特殊情况，申请逾期豁免\n预计处理时效：2小时内").processingTime("2小时内").sortOrder(9).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("贷款咨询").category("LOAN")
                .solution("1. 了解客户贷款需求与资质\n2. 推荐适合的贷款产品\n3. 引导至信贷部门进一步沟通\n预计处理时效：实时").processingTime("实时").sortOrder(10).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("密码重置").category("ACCOUNT")
                .solution("1. 核实客户身份（姓名+身份证+手机号）\n2. 引导客户通过手机银行或网点重置\n3. 如有U盾可在线重置\n预计处理时效：30分钟内").processingTime("30分钟内").sortOrder(11).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("账户冻结").category("ACCOUNT")
                .solution("1. 核实冻结原因（风控/司法/自助）\n2. 联系风控合规部确认解冻条件\n3. 指导客户准备相关证明材料\n预计处理时效：1-3个工作日").processingTime("1-3个工作日").sortOrder(12).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("理财产品").category("WEALTH")
                .solution("1. 了解客户风险偏好与投资期限\n2. 推荐适合的理财产品\n3. 引导至理财经理进一步沟通\n预计处理时效：实时").processingTime("实时").sortOrder(13).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("挂失补办").category("ACCOUNT")
                .solution("1. 确认挂失类型（临时/正式）\n2. 引导客户至就近网点办理\n3. 告知补办时限与费用\n预计处理时效：1-7个工作日").processingTime("1-7个工作日").sortOrder(14).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("ETC问题").category("VALUE_ADDED")
                .solution("1. 核实ETC设备状态与账户情况\n2. 检查绑定银行卡是否正常\n3. 联系ETC客服查询扣费异常\n预计处理时效：2小时内").processingTime("2小时内").sortOrder(15).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("商户收款").category("MERCHANT")
                .solution("1. 核实商户号与结算账户\n2. 检查交易流水与对账记录\n3. 如有短款，发起差错处理\n预计处理时效：4小时内").processingTime("4小时内").sortOrder(16).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("外汇业务").category("FOREX")
                .solution("1. 了解客户外汇购买/结汇需求\n2. 核实购汇额度与用途\n3. 引导至外币业务柜台办理\n预计处理时效：实时").processingTime("实时").sortOrder(17).build());
        solutionRepository.save(KnowledgeSolution.builder().scenario("默认").category("DEFAULT")
                .solution("1. 记录客户诉求\n2. 转派至相关业务部门\n3. 预计处理时效：24小时内").processingTime("24小时内").sortOrder(99).build());
    }

    public List<KnowledgeKeyword> getAllKeywords() {
        return keywordRepository.findAll();
    }

    public List<KnowledgeKeyword> getKeywordsByCategory(String category) {
        return keywordRepository.findByCategoryOrderBySortOrderAsc(category);
    }

    public KnowledgeKeyword saveKeyword(KnowledgeKeyword keyword) {
        return keywordRepository.save(keyword);
    }

    public void deleteKeyword(Long id) {
        keywordRepository.deleteById(id);
    }

    public List<KnowledgeSolution> getAllSolutions() {
        return solutionRepository.findAll();
    }

    public List<KnowledgeSolution> getSolutionsByCategory(String category) {
        return solutionRepository.findByCategoryOrderBySortOrderAsc(category);
    }

    public KnowledgeSolution saveSolution(KnowledgeSolution solution) {
        return solutionRepository.save(solution);
    }

    public void deleteSolution(Long id) {
        solutionRepository.deleteById(id);
    }

    public Map<String, List<String>> getKeywordMapByCategory(String category) {
        return keywordRepository.findByCategory(category).stream()
                .collect(Collectors.toMap(
                        KnowledgeKeyword::getKeywordName,
                        k -> Arrays.asList(k.getKeywords().split(","))
                ));
    }

    public Map<String, String> getSolutionMap() {
        return solutionRepository.findAll().stream()
                .collect(Collectors.toMap(
                        KnowledgeSolution::getScenario,
                        KnowledgeSolution::getSolution
                ));
    }

    private void initArticles() {
        articleRepository.save(KnowledgeArticle.builder()
                .title("跨行转账超时处理操作指引")
                .summary("详细说明跨行转账超时的排查步骤、调账流程和客户沟通话术")
                .content("## 跨行转账超时处理操作指引\n\n### 一、问题定位\n1. 登录核心银行系统查询交易状态\n2. 确认资金当前位置（过渡户/已扣款/未扣款）\n3. 检查通道状态和清算进度\n\n### 二、处理方案\n#### 方案一：发起调账（推荐）\n- 适用场景：资金已扣款但未到账\n- 操作步骤：\n  1. 在调账系统提交调账申请\n  2. 选择\"跨行转账超时\"调账类型\n  3. 填写交易流水号和金额\n  4. 提交审批\n- 预计时效：2小时内完成\n\n#### 方案二：等待自动冲正\n- 适用场景：系统正在处理中\n- 说明：如交易最终失败，系统会自动冲正\n- 等待时间：最长24小时\n\n### 三、客户沟通话术\n\"XX先生/女士您好，您反映的转账问题我们已经受理。经查询，您的资金目前在银行内部处理中，是安全的。我们已为您加急处理，预计2小时内资金将退回您的账户，届时我们会第一时间通知您。\"\n\n### 四、注意事项\n1. 及时处理，避免超时\n2. 使用标准话术，不承诺具体到账时间\n3. 做好记录，便于后续跟进")
                .category("转账汇款")
                .tags("转账,汇款,超时,调账,跨行")
                .keywords("转账,汇款,超时,调账,跨行,资金,到账")
                .author("信息科技部")
                .source("内部知识库")
                .sortOrder(1).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("资金调账申请操作手册")
                .summary("调账申请的填写规范、审批流程和时效要求")
                .content("## 资金调账申请操作手册\n\n### 一、调账申请条件\n1. 交易状态异常（超时、失败、重复扣款等）\n2. 客户投诉且资金确实存在问题\n3. 有完整的交易流水和证据\n\n### 二、申请流程\n1. 登录调账系统（地址：xxx）\n2. 选择调账类型\n3. 填写交易信息\n4. 上传相关凭证\n5. 提交审批\n\n### 三、填写规范\n- 交易流水号：完整填写\n- 金额：精确到分\n- 原因描述：简明扼要\n- 附件：交易截图、客户反馈记录\n\n### 四、审批时效\n- 紧急调账：1小时内\n- 普通调账：4小时内\n- 大额调账：需主管审批，24小时内")
                .category("转账汇款")
                .tags("调账,资金,申请,审批")
                .keywords("调账,资金,申请,审批,操作")
                .author("信息科技部")
                .source("内部知识库")
                .sortOrder(2).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("手机银行登录失败排查指南")
                .summary("详细说明手机银行登录失败的各种原因和解决方法")
                .content("## 手机银行登录失败排查指南\n\n### 一、常见原因\n\n#### 1. 密码错误\n- 连续输错5次密码，账户会被锁定24小时\n- 建议使用\"忘记密码\"功能重置\n\n#### 2. 网络问题\n- 检查网络连接是否正常\n- 尝试切换WiFi或移动数据\n- 确认网络是否稳定\n\n#### 3. 版本问题\n- 确保手机银行是最新版本\n- 卸载后重新安装\n\n#### 4. 系统兼容\n- 检查手机系统版本\n- 确保满足最低版本要求\n\n### 二、解决步骤\n1. 确认密码是否正确\n2. 检查网络连接\n3. 更新或重装APP\n4. 尝试其他设备\n5. 联系客服\n\n### 三、安全提示\n- 不要向任何人透露密码\n- 确保在安全网络环境下登录\n- 及时更新密码")
                .category("账户管理")
                .tags("登录,手机银行,密码,失败")
                .keywords("登录,手机银行,密码,失败,登录不了")
                .author("信息科技部")
                .source("内部知识库")
                .sortOrder(3).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("信用卡逾期处理规范")
                .summary("信用卡逾期的处理流程、客户沟通要点和风控要求")
                .content("## 信用卡逾期处理规范\n\n### 一、逾期分类\n- 轻度逾期：1-30天\n- 中度逾期：31-90天\n- 重度逾期：90天以上\n\n### 二、处理流程\n1. 逾期通知：到期前3天发送提醒\n2. 逾期催收：逾期后按流程催收\n3. 特殊处理：根据情况申请豁免或分期\n\n### 三、沟通要点\n- 了解逾期原因\n- 引导尽快还款\n- 说明逾期影响\n- 提供可行方案\n\n### 四、风控要求\n- 记录完整沟通内容\n- 遵守催收规范\n- 保护客户隐私")
                .category("信用卡")
                .tags("信用卡,逾期,还款,催收")
                .keywords("信用卡,逾期,还款,催收,罚息")
                .author("信用卡中心")
                .source("内部知识库")
                .sortOrder(4).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("账户冻结与解冻操作指引")
                .summary("账户冻结的原因分类、解冻流程和注意事项")
                .content("## 账户冻结与解冻操作指引\n\n### 一、冻结原因\n\n#### 1. 风控冻结\n- 异常交易触发\n- 系统自动保护\n- 可在线解冻\n\n#### 2. 司法冻结\n- 涉及法律案件\n- 配合司法调查\n- 需司法机关解冻\n\n#### 3. 自助冻结\n- 客户主动申请\n- 可在线解冻\n\n### 二、解冻流程\n1. 核实冻结原因\n2. 准备相关材料\n3. 提交解冻申请\n4. 等待审批结果\n\n### 三、注意事项\n- 风控冻结需本人办理\n- 司法冻结需配合调查\n- 及时通知客户进度")
                .category("账户管理")
                .tags("冻结,解冻,账户,风控")
                .keywords("冻结,解冻,账户,风控,司法")
                .author("风控合规部")
                .source("内部知识库")
                .sortOrder(5).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("理财产品常见问题解答")
                .summary("理财产品相关的常见问题及解答")
                .content("## 理财产品常见问题解答\n\n### Q1：理财产品的收益是多少？\nA：理财产品收益根据市场情况波动，具体以产品说明书为准。\n\n### Q2：理财产品可以随时赎回吗？\nA：开放式产品可随时赎回，封闭式产品需到期才能赎回。\n\n### Q3：理财产品会亏损吗？\nA：理财产品有风险，可能产生亏损，请仔细阅读风险提示。\n\n### Q4：理财产品到期后资金何时到账？\nA：一般T+1工作日到账，具体以产品说明为准。\n\n### Q5：如何购买理财产品？\nA：通过手机银行或网上银行购买，部分产品需到柜台办理。")
                .category("理财")
                .tags("理财,收益,赎回,风险")
                .keywords("理财,收益,赎回,风险,基金")
                .author("零售业务部")
                .source("内部知识库")
                .sortOrder(6).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("ETC业务常见问题处理")
                .summary("ETC设备故障、扣费异常等常见问题的处理方法")
                .content("## ETC业务常见问题处理\n\n### 一、设备故障\n\n#### 1. OBU设备不识别\n- 检查设备是否安装正确\n- 确认设备电量是否充足\n- 到网点检测或更换\n\n#### 2. 标签脱落\n- 重新粘贴标签\n- 到网点重新激活\n\n### 二、扣费异常\n\n#### 1. 扣费失败\n- 检查绑定账户余额\n- 确认账户状态正常\n- 联系ETC客服\n\n#### 2. 重复扣费\n- 保留通行凭证\n- 提交退款申请\n- 5-7个工作日处理\n\n### 三、办理业务\n- 绑定账户变更：到柜台或线上办理\n- 注销ETC：需无欠款，携带身份证")
                .category("增值服务")
                .tags("ETC,设备,扣费,故障")
                .keywords("ETC,设备,扣费,故障,高速")
                .author("零售业务部")
                .source("内部知识库")
                .sortOrder(7).build());

        articleRepository.save(KnowledgeArticle.builder()
                .title("商户收款常见问题处理")
                .summary("商户POS机交易、结算等常见问题的处理方法")
                .content("## 商户收款常见问题处理\n\n### 一、交易问题\n\n#### 1. 交易失败\n- 检查POS机网络\n- 确认商户状态正常\n- 联系收单机构\n\n#### 2. 交易未到账\n- 核实结算周期\n- 检查结算账户\n- 联系收单机构查询\n\n### 二、结算问题\n- T+1结算：次工作日到账\n- D+0结算：当天到账（需申请）\n\n### 三、设备维护\n- POS机故障：联系维修\n- 纸卷更换：自行更换\n- 网络问题：检查SIM卡或WiFi")
                .category("商户收单")
                .tags("商户,POS,收款,结算")
                .keywords("商户,POS,收款,结算,交易")
                .author("零售业务部")
                .source("内部知识库")
                .sortOrder(8).build());
    }

    public List<KnowledgeArticle> getAllArticles() {
        return articleRepository.findAll();
    }

    public List<KnowledgeArticle> getArticlesByCategory(String category) {
        return articleRepository.findByCategoryAndEnabledTrueOrderBySortOrderAsc(category);
    }

    public KnowledgeArticle getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    public KnowledgeArticle saveArticle(KnowledgeArticle article) {
        return articleRepository.save(article);
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    public List<KnowledgeArticle> searchArticles(String keyword) {
        return articleRepository.searchByKeyword(keyword);
    }

    public List<KnowledgeArticle> findRelatedArticles(String keyword) {
        return articleRepository.findRelatedArticles(keyword);
    }
}
