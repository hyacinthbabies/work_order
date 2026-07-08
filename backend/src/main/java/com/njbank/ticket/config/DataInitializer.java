package com.njbank.ticket.config;

import com.njbank.ticket.entity.*;
import com.njbank.ticket.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerAssigneeRepository customerAssigneeRepository;
    private final WorkOrderStatsRepository workOrderStatsRepository;
    
    public DataInitializer(UserRepository userRepository, 
                           TicketRepository ticketRepository, 
                           PasswordEncoder passwordEncoder,
                           CustomerAssigneeRepository customerAssigneeRepository,
                           WorkOrderStatsRepository workOrderStatsRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerAssigneeRepository = customerAssigneeRepository;
        this.workOrderStatsRepository = workOrderStatsRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .name("系统管理员")
                    .role("系统管理员")
                    .department("信息科技部")
                    .isActive(true)
                    .skillTags("系统管理,运维,风控")
                    .engineerLevel("高级工程师")
                    .isOnDuty(false)
                    .build();
            userRepository.save(admin);
        }
        
        if (!userRepository.existsByUsername("kefu01")) {
            User kefu01 = User.builder()
                    .username("kefu01")
                    .password(passwordEncoder.encode("kefu123"))
                    .name("张三")
                    .role("一线客服")
                    .department("客服中心")
                    .isActive(true)
                    .skillTags("账户查询,账单疑问,业务咨询")
                    .engineerLevel("初级工程师")
                    .isOnDuty(true)
                    .build();
            userRepository.save(kefu01);
        }
        
        if (!userRepository.existsByUsername("kefu02")) {
            User kefu02 = User.builder()
                    .username("kefu02")
                    .password(passwordEncoder.encode("kefu123"))
                    .name("李四")
                    .role("一线客服")
                    .department("客服中心")
                    .isActive(true)
                    .skillTags("挂失,开户,转账")
                    .engineerLevel("初级工程师")
                    .isOnDuty(false)
                    .build();
            userRepository.save(kefu02);
        }
        
        if (!userRepository.existsByUsername("kefu03")) {
            User kefu03 = User.builder()
                    .username("kefu03")
                    .password(passwordEncoder.encode("kefu123"))
                    .name("王五")
                    .role("一线客服")
                    .department("零售业务部")
                    .isActive(true)
                    .skillTags("理财,账户查询,投诉处理")
                    .engineerLevel("中级工程师")
                    .isOnDuty(true)
                    .build();
            userRepository.save(kefu03);
        }
        
        if (!userRepository.existsByUsername("gaoji01")) {
            User gaoji01 = User.builder()
                    .username("gaoji01")
                    .password(passwordEncoder.encode("gaoji123"))
                    .name("赵六")
                    .role("高级工程师")
                    .department("客服中心")
                    .isActive(true)
                    .skillTags("投诉处理,服务态度,风控,异常交易")
                    .engineerLevel("高级工程师")
                    .isOnDuty(false)
                    .build();
            userRepository.save(gaoji01);
        }
        
        if (!userRepository.existsByUsername("zhuguan01")) {
            User zhuguan01 = User.builder()
                    .username("zhuguan01")
                    .password(passwordEncoder.encode("zhuguan123"))
                    .name("钱七")
                    .role("客服主管")
                    .department("客服中心")
                    .isActive(true)
                    .skillTags("投诉处理,账户查询,账单疑问,业务咨询")
                    .engineerLevel("高级工程师")
                    .isOnDuty(true)
                    .build();
            userRepository.save(zhuguan01);
        }
        
        if (!userRepository.existsByUsername("zhuguan02")) {
            User zhuguan02 = User.builder()
                    .username("zhuguan02")
                    .password(passwordEncoder.encode("zhuguan123"))
                    .name("孙八")
                    .role("客服主管")
                    .department("零售业务部")
                    .isActive(true)
                    .skillTags("理财,风控,异常交易")
                    .engineerLevel("高级工程师")
                    .isOnDuty(false)
                    .build();
            userRepository.save(zhuguan02);
        }
        
        if (ticketRepository.count() == 0) {
            List<Ticket> tickets = Arrays.asList(
                    Ticket.builder()
                            .ticketNo("WO202505280001")
                            .title("账户余额异常查询")
                            .description("客户反映账户余额与实际不符，需要核对交易记录")
                            .type("咨询类")
                            .urgency("high")
                            .channel("手机银行")
                            .department("客服中心")
                            .status("待派单")
                            .customerNo("C20250001")
                            .customerName("周九")
                            .phone("13812345678")
                            .accountNo("6222021234567890123")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(1))
                            .slaEndTime(LocalDateTime.now().plusHours(1))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280002")
                            .title("信用卡账单疑问")
                            .description("客户对本月信用卡账单有疑问，要求解释消费明细")
                            .type("咨询类")
                            .urgency("medium")
                            .channel("电话银行")
                            .department("客服中心")
                            .status("待派单")
                            .customerNo("C20250002")
                            .customerName("吴十")
                            .phone("13987654321")
                            .accountNo("6225881234567890")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(2))
                            .slaEndTime(LocalDateTime.now().plusHours(2))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280003")
                            .title("投诉服务态度问题")
                            .description("客户投诉网点柜员服务态度恶劣，要求处理")
                            .type("投诉类")
                            .urgency("high")
                            .channel("网点")
                            .department("客服中心")
                            .status("待派单")
                            .customerNo("C20250003")
                            .customerName("郑十一")
                            .phone("13611112222")
                            .accountNo("6222029876543210987")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(3))
                            .slaEndTime(LocalDateTime.now().minusHours(1))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280004")
                            .title("挂失银行卡")
                            .description("客户银行卡丢失，要求立即挂失")
                            .type("办理类")
                            .urgency("high")
                            .channel("电话银行")
                            .department("零售业务部")
                            .status("已完成")
                            .customerNo("C20250004")
                            .customerName("王十二")
                            .phone("13733334444")
                            .accountNo("622202555566667777")
                            .createdBy("kefu01")
                            .assignedTo("kefu02")
                            .createTime(LocalDateTime.now().minusDays(1))
                            .updateTime(LocalDateTime.now().minusDays(1).plusHours(1))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280005")
                            .title("大额交易异常预警")
                            .description("系统检测到异常大额交易，需要风控确认")
                            .type("风控类")
                            .urgency("high")
                            .channel("系统")
                            .department("风控合规部")
                            .status("待派单")
                            .customerNo("C20250005")
                            .customerName("赵十三")
                            .phone("13855556666")
                            .accountNo("622202888899990000")
                            .createdBy("admin")
                            .createTime(LocalDateTime.now().minusHours(4))
                            .slaEndTime(LocalDateTime.now().minusHours(2))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280006")
                            .title("网上银行登录问题")
                            .description("客户无法登录网上银行，密码重置后仍无法登录")
                            .type("咨询类")
                            .urgency("medium")
                            .channel("网上银行")
                            .department("信息科技部")
                            .status("待派单")
                            .customerNo("C20250006")
                            .customerName("钱十四")
                            .phone("13977778888")
                            .accountNo("622202111122223333")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(5))
                            .slaEndTime(LocalDateTime.now().minusHours(1))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280007")
                            .title("理财产品赎回")
                            .description("客户要求赎回理财产品，金额较大需要审批")
                            .type("办理类")
                            .urgency("medium")
                            .channel("手机银行")
                            .department("零售业务部")
                            .status("待派单")
                            .customerNo("C20250007")
                            .customerName("孙十五")
                            .phone("13599990000")
                            .accountNo("622202444455556666")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(6))
                            .slaEndTime(LocalDateTime.now().plusHours(2))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280008")
                            .title("短信通知异常")
                            .description("客户收不到账户交易短信通知")
                            .type("咨询类")
                            .urgency("low")
                            .channel("微信银行")
                            .department("信息科技部")
                            .status("待派单")
                            .customerNo("C20250008")
                            .customerName("李十六")
                            .phone("13488889999")
                            .accountNo("622202777788889999")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(7))
                            .slaEndTime(LocalDateTime.now().plusHours(1))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280009")
                            .title("转账失败处理")
                            .description("客户转账失败，资金未到账，需要处理")
                            .type("办理类")
                            .urgency("high")
                            .channel("手机银行")
                            .department("零售业务部")
                            .status("待派单")
                            .customerNo("C20250009")
                            .customerName("周十七")
                            .phone("13377776666")
                            .accountNo("622202666655554444")
                            .createdBy("kefu02")
                            .createTime(LocalDateTime.now().minusHours(8))
                            .slaEndTime(LocalDateTime.now().minusHours(6))
                            .build(),
                    Ticket.builder()
                            .ticketNo("WO202505280010")
                            .title("账户冻结解冻")
                            .description("客户账户被冻结，需要核实并解冻")
                            .type("办理类")
                            .urgency("high")
                            .channel("柜台")
                            .department("零售业务部")
                            .status("待派单")
                            .customerNo("C20250001")
                            .customerName("周九")
                            .phone("13812345678")
                            .accountNo("6222021234567890123")
                            .createdBy("kefu03")
                            .createTime(LocalDateTime.now().minusHours(9))
                            .slaEndTime(LocalDateTime.now().minusHours(7))
                            .build()
            );
            ticketRepository.saveAll(tickets);
        }
        
        if (customerAssigneeRepository.count() == 0) {
            List<CustomerAssignee> cas = Arrays.asList(
                    CustomerAssignee.builder()
                            .customerNo("C20250001")
                            .assigneeUsername("kefu03")
                            .build(),
                    CustomerAssignee.builder()
                            .customerNo("C20250002")
                            .assigneeUsername("kefu01")
                            .build(),
                    CustomerAssignee.builder()
                            .customerNo("C20250003")
                            .assigneeUsername("gaoji01")
                            .build(),
                    CustomerAssignee.builder()
                            .customerNo("C20250004")
                            .assigneeUsername("kefu02")
                            .build()
            );
            customerAssigneeRepository.saveAll(cas);
        }
        
        if (workOrderStatsRepository.count() == 0) {
            List<WorkOrderStats> stats = Arrays.asList(
                    WorkOrderStats.builder()
                            .username("kefu01")
                            .ticketType("咨询类")
                            .handleCount(50)
                            .avgHandleTime(15.0)
                            .praiseRate(0.95)
                            .build(),
                    WorkOrderStats.builder()
                            .username("kefu01")
                            .ticketType("办理类")
                            .handleCount(20)
                            .avgHandleTime(25.0)
                            .praiseRate(0.88)
                            .build(),
                    WorkOrderStats.builder()
                            .username("kefu02")
                            .ticketType("办理类")
                            .handleCount(60)
                            .avgHandleTime(12.0)
                            .praiseRate(0.92)
                            .build(),
                    WorkOrderStats.builder()
                            .username("kefu02")
                            .ticketType("咨询类")
                            .handleCount(15)
                            .avgHandleTime(18.0)
                            .praiseRate(0.85)
                            .build(),
                    WorkOrderStats.builder()
                            .username("kefu03")
                            .ticketType("咨询类")
                            .handleCount(30)
                            .avgHandleTime(10.0)
                            .praiseRate(0.98)
                            .build(),
                    WorkOrderStats.builder()
                            .username("kefu03")
                            .ticketType("投诉类")
                            .handleCount(10)
                            .avgHandleTime(30.0)
                            .praiseRate(0.80)
                            .build(),
                    WorkOrderStats.builder()
                            .username("kefu03")
                            .ticketType("办理类")
                            .handleCount(25)
                            .avgHandleTime(18.0)
                            .praiseRate(0.90)
                            .build(),
                    WorkOrderStats.builder()
                            .username("gaoji01")
                            .ticketType("投诉类")
                            .handleCount(80)
                            .avgHandleTime(20.0)
                            .praiseRate(0.96)
                            .build(),
                    WorkOrderStats.builder()
                            .username("gaoji01")
                            .ticketType("风控类")
                            .handleCount(40)
                            .avgHandleTime(25.0)
                            .praiseRate(0.94)
                            .build(),
                    WorkOrderStats.builder()
                            .username("zhuguan01")
                            .ticketType("咨询类")
                            .handleCount(100)
                            .avgHandleTime(8.0)
                            .praiseRate(0.99)
                            .build(),
                    WorkOrderStats.builder()
                            .username("zhuguan01")
                            .ticketType("投诉类")
                            .handleCount(30)
                            .avgHandleTime(22.0)
                            .praiseRate(0.97)
                            .build()
            );
            workOrderStatsRepository.saveAll(stats);
        }
    }
}