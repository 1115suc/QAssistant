package course.QAssistant.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.pojo.dto.StudySessionEndDTO;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.StudyDailySummary;
import course.QAssistant.pojo.vo.request.DateRangeVO;
import course.QAssistant.pojo.vo.request.GoldenPeriodQueryVO;
import course.QAssistant.pojo.vo.response.GoldenPeriodVO;
import course.QAssistant.pojo.vo.response.HeatmapItemVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.SummaryTrendVO;
import course.QAssistant.service.StudyDailySummaryService;
import course.QAssistant.mapper.StudyDailySummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyDailySummaryServiceImpl extends ServiceImpl<StudyDailySummaryMapper, StudyDailySummary>
        implements StudyDailySummaryService {

    private final RedisComponent redisComponent;

    /**
     * 同步会话结束时的每日总结数据
     *
     * @param dto 会话结束 DTO，包含用户 ID、开始时间、专注时长、休息时长、类别
     */
    public void syncSummaryOnSessionEnd(StudySessionEndDTO dto) {
        LocalDate date = dto.getStartTime().toLocalDate();

        StudyDailySummary summary = lambdaQuery()
                .eq(StudyDailySummary::getUserId, dto.getUserId())
                .eq(StudyDailySummary::getStudyDate, date)
                .one();

        if (summary == null) {
            // 新建
            summary = new StudyDailySummary();
            summary.setUserId(dto.getUserId());
            summary.setStudyDate(date);
            summary.setTotalStudyMinutes(dto.getFocusMinutes());
            summary.setTotalRestMinutes(dto.getRestMinutes());
            summary.setSessionCount(1);
            Map<String, Integer> catMap = new HashMap<>();
            catMap.put(dto.getCategory(), dto.getFocusMinutes());
            summary.setCategoryMinutes(JSONUtil.toJsonStr(catMap));
            save(summary);
        } else {
            // 累加
            summary.setTotalStudyMinutes(summary.getTotalStudyMinutes() + dto.getFocusMinutes());
            summary.setTotalRestMinutes(summary.getTotalRestMinutes() + dto.getRestMinutes());
            summary.setSessionCount(summary.getSessionCount() + 1);
            // 更新 category_minutes
            Map<String, Integer> catMap = JSONUtil.toBean(summary.getCategoryMinutes(), HashMap.class);
            if (catMap.containsKey(dto.getCategory())) {
                catMap.put(dto.getCategory(), catMap.get(dto.getCategory()) + dto.getFocusMinutes());
            } else {
                catMap.put(dto.getCategory(), dto.getFocusMinutes());
            }
            summary.setCategoryMinutes(JSONUtil.toJsonStr(catMap));
            updateById(summary);
        }
    }

    @Override
    public R<GoldenPeriodVO> getGoldenPeriod(GoldenPeriodQueryVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        List<Map<String, Object>> rows = baseMapper.selectGoldenPeriod(
                userId, vo.getStartDate(), vo.getEndDate()
        );

        // 初始化四个时段均为0
        int[] minutes = new int[4];
        for (Map<String, Object> row : rows) {
            int period = ((Number) row.get("period")).intValue();
            int total  = ((Number) row.get("totalMinutes")).intValue();
            minutes[period] = total;
        }

        GoldenPeriodVO result = new GoldenPeriodVO();
        result.setDawnMinutes(minutes[0]);
        result.setMorningMinutes(minutes[1]);
        result.setAfternoonMinutes(minutes[2]);
        result.setEveningMinutes(minutes[3]);
        return R.ok(result);
    }


    // 获取学习趋势
    @Override
    public R<List<SummaryTrendVO>> getTrend(DateRangeVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        List<StudyDailySummary> list = lambdaQuery()
                .eq(StudyDailySummary::getUserId, userId)
                .between(StudyDailySummary::getStudyDate, vo.getStartDate(), vo.getEndDate())
                .orderByAsc(StudyDailySummary::getStudyDate)
                .list();

        List<SummaryTrendVO> result = list.stream().map(s -> {
            SummaryTrendVO t = new SummaryTrendVO();
            t.setStudyDate(s.getStudyDate());
            t.setTotalStudyMinutes(s.getTotalStudyMinutes());
            t.setCategoryMinutes(JSONUtil.toBean(s.getCategoryMinutes(), Map.class));
            return t;
        }).collect(Collectors.toList());
        return R.ok(result);
    }


    // 获取学习热力图
    @Override
    public R<List<HeatmapItemVO>> getHeatmap(DateRangeVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        List<StudyDailySummary> list = lambdaQuery()
                .eq(StudyDailySummary::getUserId, userId)
                .between(StudyDailySummary::getStudyDate, vo.getStartDate(), vo.getEndDate())
                .select(StudyDailySummary::getStudyDate, StudyDailySummary::getTotalStudyMinutes)
                .orderByAsc(StudyDailySummary::getStudyDate)
                .list();

        List<HeatmapItemVO> result = list.stream().map(s -> {
            HeatmapItemVO h = new HeatmapItemVO();
            h.setStudyDate(s.getStudyDate());
            h.setTotalStudyMinutes(s.getTotalStudyMinutes());
            return h;
        }).collect(Collectors.toList());
        return R.ok(result);
    }
}




