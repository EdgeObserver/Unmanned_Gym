package record.service;


import pojo.RecordPageParam;
import pojo.ResultMsg;

public interface ActionRecordService {
    /**
     * 添加一条行动记录
     */
    ResultMsg arrive(int id);

    /**
     * 根据ID删除行动记录
     */
    ResultMsg deleteActionRecordById(int id);

    ResultMsg leave(int id);


    /**
     * 根据ID查询行动记录
     */
    ResultMsg getActionRecordById(int id);

    /**
     * 查询所有行动记录
     */
    ResultMsg findAllPage(int pageNum, int pageSize);
    ResultMsg getList(RecordPageParam recordPageParam);
    
    /**
     * 获取今日访问人数
     */
    ResultMsg getTodayVisitCount();
    
    /**
     * 获取近7天每日访问人数统计
     */
    ResultMsg getWeeklyVisitStatistics();
}
