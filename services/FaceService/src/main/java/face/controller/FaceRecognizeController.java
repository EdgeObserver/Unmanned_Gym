package face.controller;

import face.service.FaceRecognizeService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.ResultMsg;

@RestController
public class FaceRecognizeController {

    @Autowired
    private FaceRecognizeService faceRecognizeService;

    @PostMapping("/check-in")
    public ResultMsg<String> checkInFaceId(@RequestBody FacePushRequest request) {
        System.out.println("========== 收到Python人脸推送 ==========");
        System.out.println("用户ID: " + request.getData());
        return faceRecognizeService.checkInFaceId(request.getData());
    }

    @PostMapping("/check-out")
    public ResultMsg<String> checkOutFaceId(@RequestParam int id) {
        return faceRecognizeService.checkOutFaceId(id);
    }

    @GetMapping("/current-count")
    public ResultMsg<Integer> getCurrentCount() {
        return ResultMsg.success(0, "功能待实现");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class FacePushRequest {
        private Integer code;
        private String msg;
        private Integer data;
    }
}
