package resource.service;

import pojo.Coach;
import pojo.ResultMsg;
import resource.dto.CoachDto;

public interface CoachService {
    ResultMsg findAllCoach();

    ResultMsg findAllCoachPage(Integer pageNum, Integer pageSize);

    ResultMsg getCoachById(int id);

    ResultMsg addCoach(CoachDto coachDto);

    ResultMsg updateCoach(Coach coach);

    ResultMsg deleteCoach(int id);

    ResultMsg getByName(String name);
}
