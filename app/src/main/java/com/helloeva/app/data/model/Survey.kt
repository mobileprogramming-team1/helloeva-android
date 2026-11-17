package com.helloeva.app.data.model

/** OPIc background survey (배경 설문): at least 12 items across all pages. */
const val SURVEY_MIN_TOTAL = 12

data class SurveyPage(
    val id: String,
    val category: String,          // 직업, 학생 여부, ...
    val question: String,
    val options: List<String>,
    val multiSelect: Boolean,
    val minSelect: Int = 1,
    /** Choosing this clears every other option on the page (and vice versa). */
    val exclusiveOption: String? = null,
)

object SurveyData {
    val pages = listOf(
        SurveyPage(
            id = "job",
            category = "직업",
            question = "현재 귀하는 어느 분야에\n종사하고 계십니까?",
            options = listOf("사업/회사 근무", "재택근무/재택사업", "교사/교육자", "일 경험 없음"),
            multiSelect = false,
        ),
        SurveyPage(
            id = "student",
            category = "학생 여부",
            question = "현재 학생이십니까?",
            options = listOf("예", "아니요"),
            multiSelect = false,
        ),
        SurveyPage(
            id = "course",
            category = "최근 수강 강의",
            question = "최근에 어떤 강의를\n수강하셨습니까?",
            options = listOf("학위 과정 수업", "전문 기술 향상을 위한 평생학습", "어학 수업", "수강 후 5년 이상 지남"),
            multiSelect = false,
        ),
        SurveyPage(
            id = "housing",
            category = "거주 형태",
            question = "현재 어디에\n살고 계십니까?",
            options = listOf(
                "개인 주택·아파트에 혼자 거주",
                "친구·룸메이트와 주택·아파트에 거주",
                "가족(배우자/자녀/기타 가족)과 주택·아파트에 거주",
                "학교 기숙사",
                "군대 막사·군 시설",
            ),
            multiSelect = false,
        ),
        SurveyPage(
            id = "leisure",
            category = "여가 활동",
            question = "여가 시간에 주로\n무엇을 하십니까?",
            options = listOf(
                "영화 보기", "클럽/나이트클럽 가기", "공연 보기", "콘서트 보기", "박물관 가기", "공원 가기",
                "캠핑하기", "해변 가기", "스포츠 관람", "게임하기", "SNS에 글 올리기", "친구들과 문자 대화하기",
                "주거 개선", "술집/바에 가기", "카페/커피 전문점 가기", "쇼핑하기", "TV 시청하기",
                "리얼리티 쇼 시청하기", "뉴스 보기/듣기", "요리 관련 프로그램 시청하기", "체스하기", "당구",
                "시험 대비 과정 수강하기", "구직 활동하기", "자원봉사하기", "드라이브하기",
            ),
            multiSelect = true,
            minSelect = 2,
        ),
        SurveyPage(
            id = "hobby",
            category = "취미·관심사",
            question = "취미나 관심사는\n무엇입니까?",
            options = listOf(
                "아이에게 책 읽어주기", "음악 감상하기", "악기 연주하기", "혼자 노래 부르기/합창하기", "춤추기",
                "글쓰기(편지, 단문, 시 등)", "그림 그리기", "요리하기", "반려동물 키우기", "주식 투자하기",
                "신문 읽기", "여행 관련 잡지·블로그 읽기", "독서", "사진 촬영하기",
            ),
            multiSelect = true,
        ),
        SurveyPage(
            id = "sport",
            category = "운동",
            question = "어떤 운동을\n즐기십니까?",
            options = listOf(
                "농구", "야구/소프트볼", "축구", "미식축구", "하키", "크리켓", "골프", "배구", "테니스",
                "배드민턴", "탁구", "수영", "자전거", "스키/스노보드", "아이스 스케이트", "조깅", "걷기",
                "요가", "하이킹/트레킹", "낚시", "헬스", "태권도", "운동 수업 수강하기", "운동을 전혀 하지 않음",
            ),
            multiSelect = true,
            exclusiveOption = "운동을 전혀 하지 않음",
        ),
        SurveyPage(
            id = "travel",
            category = "휴가·출장",
            question = "어떤 휴가나 출장을\n다녀오셨습니까?",
            options = listOf("집에서 보내는 휴가", "국내 출장", "해외 출장", "국내 여행", "해외 여행"),
            multiSelect = true,
        ),
    )
}
