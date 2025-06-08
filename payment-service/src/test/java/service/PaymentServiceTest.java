package service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.service.PaymentService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = PaymentService.class)
public class PaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Test
    void testGetBalance() {
        Double balanceFirstQuery = paymentService.getBalance("user").block();
        assertNotNull(balanceFirstQuery, "After query balance can't be null");
        Double balanceSecondQuery = paymentService.getBalance("user").block();
        assertEquals(balanceFirstQuery, balanceSecondQuery, "After next query balance should be the same");
    }

    @Test
    void testDoPayment() {
        Double balanceFirstQuery = paymentService.getBalance("user").block();
        double payment = balanceFirstQuery - 1;
        paymentService.doPayment(payment, "user");
        Double balanceSecondQuery = paymentService.getBalance("user").block();
        assertEquals(balanceSecondQuery, balanceFirstQuery - payment,
                "After payment balance wasn't correctly decreased");
    }

}