package face.service.impl;

import face.dto.FaceEventMessage;
import face.feign.OrderFeignClient;
import face.feign.UserFeignClient;
import face.service.FaceRecognizeService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.Order;
import pojo.ResultMsg;
import pojo.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class FaceRecognizeServiceImpl implements FaceRecognizeService {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_FACE_EVENT = "face.event.exchange";
    private static final String ROUTING_KEY_CHECKIN = "face.checkin";
    private static final String ROUTING_KEY_CHECKOUT = "face.checkout";

    @Override
    public ResultMsg<String> checkInFaceId(Integer id) {
        Order order=orderFeignClient.getLegalOrderbyUid(id).getData();
        User user=userFeignClient.getById(id).getData();
        if(order==null){
            return ResultMsg.fail(user.getUsername(), "您的订单已过期");
        }
        if (order.getOrderEndTime().isBefore(LocalDate.now())) {
            order.setStatus(0);
            orderFeignClient.update(order);
            return ResultMsg.fail(user.getUsername(), "您的订单已过期");
        }

        int remainingDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), order.getOrderEndTime()) + 1;

        System.out.println(user.toString());
        if(user.isInPlace()){
            return ResultMsg.fail(user.getUsername(),"禁止重复刷脸");
        }
        else{
            user.setInPlace(true);
            userFeignClient.update(user);

            FaceEventMessage message = new FaceEventMessage(
                    id,
                    "CHECK_IN",
                    LocalDateTime.now(),
                    user.getUsername()
            );

            try {
                System.out.println("========== 准备发送签到消息 ==========");
                System.out.println("交换机: " + EXCHANGE_FACE_EVENT);
                System.out.println("路由键: " + ROUTING_KEY_CHECKIN);
                System.out.println("消息内容: userId=" + message.getUserId() + ", username=" + message.getUsername());

                rabbitTemplate.convertAndSend(EXCHANGE_FACE_EVENT, ROUTING_KEY_CHECKIN, message);

                System.out.println("消息发送成功");
                System.out.println("======================================");
            } catch (Exception e) {
                System.err.println("消息发送失败: " + e.getMessage());
                e.printStackTrace();
            }

            return ResultMsg.success(user.getUsername(),"刷脸成功，剩余天数："+String.valueOf(remainingDays));
        }
    }

    public ResultMsg<String> checkOutFaceId(Integer id) {
        User user=userFeignClient.getById(id).getData();
        if(!user.isInPlace()){
            return ResultMsg.fail(user.getUsername(),"禁止重复刷脸");
        }
        else{
            user.setInPlace(false);
            userFeignClient.update(user);

            FaceEventMessage message = new FaceEventMessage(
                    id,
                    "CHECK_OUT",
                    LocalDateTime.now(),
                    user.getUsername()
            );

            try {
                System.out.println("========== 准备发送签退消息 ==========");
                System.out.println("交换机: " + EXCHANGE_FACE_EVENT);
                System.out.println("路由键: " + ROUTING_KEY_CHECKOUT);
                System.out.println("消息内容: userId=" + message.getUserId() + ", username=" + message.getUsername());

                rabbitTemplate.convertAndSend(EXCHANGE_FACE_EVENT, ROUTING_KEY_CHECKOUT, message);

                System.out.println("消息发送成功");
                System.out.println("======================================");
            } catch (Exception e) {
                System.err.println("消息发送失败: " + e.getMessage());
                e.printStackTrace();
            }

            return ResultMsg.success(user.getUsername(),"刷脸成功");
        }
    }
}
