package com.njbank.ticket.service;

import com.njbank.ticket.entity.KnowledgeKeyword;
import com.njbank.ticket.entity.KnowledgeSolution;
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

    public KnowledgeBaseService(KnowledgeKeywordRepository keywordRepository,
                                KnowledgeSolutionRepository solutionRepository) {
        this.keywordRepository = keywordRepository;
        this.solutionRepository = solutionRepository;
    }

    @PostConstruct
    public void initDefaultData() {
        if (keywordRepository.count() == 0) {
            initKeywords();
        }
        if (solutionRepository.count() == 0) {
            initSolutions();
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
}
