package com.njbank.ticket.config;

import com.njbank.ticket.entity.Ticket;
import com.njbank.ticket.entity.User;
import com.njbank.ticket.repository.TicketRepository;
import com.njbank.ticket.repository.UserRepository;
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
    
    public DataInitializer(UserRepository userRepository, TicketRepository ticketRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
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
                    .build();
            userRepository.save(admin);
        }
        
        if (!userRepository.existsByUsername("kefu01")) {
            User kefu = User.builder()
                    .username("kefu01")
                    .password(passwordEncoder.encode("kefu123"))
                    .name("张三")
                    .role("一线客服")
                    .department("客服中心")
                    .isActive(true)
                    .build();
            userRepository.save(kefu);
        }
        
        if (!userRepository.existsByUsername("zhuguan01")) {
            User zhuguan = User.builder()
                    .username("zhuguan01")
                    .password(passwordEncoder.encode("zhuguan123"))
                    .name("李四")
                    .role("客服主管")
                    .department("客服中心")
                    .isActive(true)
                    .build();
            userRepository.save(zhuguan);
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
                            .department("零售业务部")
                            .status("处理中")
                            .customerNo("C20250001")
                            .customerName("王五")
                            .phone("13812345678")
                            .accountNo("6222021234567890123")
                            .createdBy("kefu01")
                            .assignedTo("kefu01")
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
                            .department("信用卡中心")
                            .status("待处理")
                            .customerNo("C20250002")
                            .customerName("赵六")
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
                            .department("零售业务部")
                            .status("待处理")
                            .customerNo("C20250003")
                            .customerName("孙七")
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
                            .customerName("周八")
                            .phone("13733334444")
                            .accountNo("622202555566667777")
                            .createdBy("kefu01")
                            .assignedTo("kefu01")
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
                            .status("处理中")
                            .customerNo("C20250005")
                            .customerName("吴九")
                            .phone("13855556666")
                            .accountNo("622202888899990000")
                            .createdBy("admin")
                            .assignedTo("admin")
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
                            .status("待处理")
                            .customerNo("C20250006")
                            .customerName("郑十")
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
                            .status("待审批")
                            .customerNo("C20250007")
                            .customerName("钱十一")
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
                            .status("待处理")
                            .customerNo("C20250008")
                            .customerName("刘十二")
                            .phone("13488889999")
                            .accountNo("622202777788889999")
                            .createdBy("kefu01")
                            .createTime(LocalDateTime.now().minusHours(7))
                            .slaEndTime(LocalDateTime.now().plusHours(1))
                            .build()
            );
            ticketRepository.saveAll(tickets);
        }
    }
}