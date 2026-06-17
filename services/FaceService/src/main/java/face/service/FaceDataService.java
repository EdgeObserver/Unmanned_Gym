package face.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "python-face-data", url = "http://localhost:5050")
public interface FaceDataService {
    @PostMapping("/py/face/build")
    Map<String, String> Build();
//    Map<String, String> OpenSupervision();
}
