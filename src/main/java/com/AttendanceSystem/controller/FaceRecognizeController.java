package com.AttendanceSystem.controller;

import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.service.face.FacevisionService;
import com.AttendanceSystem.service.face.impl.FaceRecognizeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/face")
public class FaceRecognizeController {
    @Autowired
    private FaceRecognizeServiceImpl frs;

    @PostMapping("/in")
    public ResultMsg<String> checkInFaceId(@RequestBody ResultMsg<Integer> msg){
        int id=msg.getData();
        return frs.checkInFaceId(id);
    }
    @PostMapping("/out")
    public ResultMsg<String> checkOutFaceId(@RequestBody ResultMsg<Integer> msg){
        int id=msg.getData();
        return frs.checkOutFaceId(id);
    }


}
