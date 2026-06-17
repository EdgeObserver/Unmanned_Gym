package face.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "python-face-vision", url = "http://localhost:5000")
public interface FacevisionService {

    @PostMapping("/py/face/start")
    Map<String, String> recognize();
}
