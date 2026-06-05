package com.employee.dto;

import lombok.Data;

import java.util.List;

@Data
public class BirthdayStatisticsVO {

    private List<MonthCountVO> birthdayDistribution;
    private List<DeptCountVO> deptDistribution;
    private Integer yearWishCount;
    private List<RankVO> activeWishers;
    private List<RankVO> popularStars;
    private Integer yearPartyCount;
    private Double avgParticipationRate;

    @Data
    public static class MonthCountVO {
        private String month;
        private Integer count;
    }

    @Data
    public static class DeptCountVO {
        private String department;
        private Integer count;
    }

    @Data
    public static class RankVO {
        private Long id;
        private String name;
        private String avatar;
        private Integer count;
    }
}
