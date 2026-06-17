package resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.Equipment;
import pojo.ResultMsg;
import resource.dto.ResourceDto;
import resource.mapper.EquipmentMapper;
import resource.service.EquipmentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {
    @Autowired
    private EquipmentMapper equipmentMapper;
    @Override
    public ResultMsg findAllResource() {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        List<Equipment> equipment = equipmentMapper.selectList(queryWrapper);
        return ResultMsg.success(equipment, "查找成功");
    }

    @Override
    public ResultMsg findAllResourcePage(Integer pageNum, Integer pageSize) {
        Page<Equipment> page = new Page<>(pageNum, pageSize);
        //把所有isDelete为“n”的数据筛选出来并且分页
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        page = equipmentMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }

    @Override
    public ResultMsg getResourceById(int id) {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("id", id);
//        System.out.println(id);
        Equipment equipment = equipmentMapper.selectOne(queryWrapper);

        if (equipment != null) {
            return ResultMsg.success(equipment, "操作成功");
        }
        return ResultMsg.fail(null, "用户不存在");
    }

    @Override
    public ResultMsg addResource(ResourceDto dto) {
        String name = dto.getName();
        // 检查用户名是否已存在，如果存在则不能注册
        if(getByName(name).getData() != null){
            return ResultMsg.fail(null, "资源已存在");
        }
        // ① 先插入，拿到自增主键（MyBatis-Plus 默认返回实体里已赋值）
        Equipment equipment = new Equipment();

        equipment.setName(dto.getName());
        equipment.setNum(dto.getNum());
        equipment.setNeedCoach((dto.getNeedCoach()));
        equipmentMapper.insert(equipment);          // id 已被填充

        return ResultMsg.success("","添加成功");
    }

    @Override
    public ResultMsg updateResource(Equipment equipment) {
        equipmentMapper.updateById(equipment);
        return ResultMsg.success(null, "更新成功");
    }

    @Override
    public ResultMsg deleteResource(int id) {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("id", id);
        Equipment equipment = equipmentMapper.selectOne(queryWrapper);

        if (equipment == null) {
            return ResultMsg.fail(null, "资源不存在");
        }

        equipment.setIsDeleted("y");
        equipment.setUpdatedTime(LocalDateTime.now());
        equipmentMapper.updateById(equipment);

        return ResultMsg.success(null, "删除成功");
    }
    @Override
    public ResultMsg getByName(String name) {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("name", name);
        Equipment equipment = equipmentMapper.selectOne(queryWrapper);
        return ResultMsg.success(equipment,"查询成功");
    }
}
