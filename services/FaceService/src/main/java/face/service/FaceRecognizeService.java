package face.service;
import pojo.ResultMsg;

public interface FaceRecognizeService {
    public ResultMsg<String> checkInFaceId(Integer id);
    public ResultMsg<String> checkOutFaceId(Integer id);

}
