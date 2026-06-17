package user.service;


import org.springframework.web.multipart.MultipartFile;
import pojo.ResultMsg;
import pojo.User;
import pojo.UserPageParam;
import user.dto.UserDto;

public interface UserService {
    public ResultMsg login(String userId, String password);
    public ResultMsg register(UserDto dto, MultipartFile avatarFile);
    public ResultMsg findAll();

    public ResultMsg findAllPage(Integer pageNum, Integer pageSize);

    public ResultMsg update(User sysUser);

    public ResultMsg getById(int userId);
    public ResultMsg deleteById(int id);

    public ResultMsg deleteByBatchIds(int[] ids);

    public ResultMsg getList(UserPageParam sysUserPageParam);

    public ResultMsg info();

    public ResultMsg getIdByUserId(String userId);

   public ResultMsg getByUsername(String userId);

    public ResultMsg checkOrder(Integer id);

    ResultMsg managerLogin(String userId, String password);
}


