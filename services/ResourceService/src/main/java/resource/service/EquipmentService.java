package resource.service;

import pojo.Equipment;
import pojo.ResultMsg;
import resource.dto.ResourceDto;

public interface EquipmentService {
    ResultMsg findAllResource();

    ResultMsg findAllResourcePage(Integer pageNum, Integer pageSize);

    ResultMsg getResourceById(int id);

    ResultMsg addResource(ResourceDto resourceDto);

    ResultMsg updateResource(Equipment equipment);

    ResultMsg deleteResource(int id);

    ResultMsg getByName(String username);
}
