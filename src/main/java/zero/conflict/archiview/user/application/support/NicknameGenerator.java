package zero.conflict.archiview.user.application.support;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "행복한", "즐거운", "용감한", "조용한", "활기찬", "똑똑한", "귀여운", "멋진",
            "배고픈", "졸린", "빠른", "느린", "상냥한", "시크한", "엉뚱한", "자유로운",
            "신비로운", "차분한", "따뜻한", "차가운", "열정적인", "소심한", "대담한", "유쾌한");

    private static final List<String> ANIMALS = List.of(
            "사자", "호랑이", "토끼", "강아지", "고양이", "곰", "여우", "늑대",
            "코끼리", "기린", "펭귄", "판다", "독수리", "거북이", "다람쥐", "오리",
            "병아리", "햄스터", "부엉이", "고슴도치", "돌고래", "물범", "알파카", "수달", "염소");

    private final Random random = new Random();

    public String generate() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        return adjective + " " + animal;
    }
}
