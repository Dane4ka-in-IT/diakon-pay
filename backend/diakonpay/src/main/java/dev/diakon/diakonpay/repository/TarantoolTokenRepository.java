package dev.diakon.diakonpay.repository;

import io.tarantool.client.TarantoolClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TarantoolTokenRepository {

    private final TarantoolClient client;

    public void saveToken(String jti, Integer userId, String tokenType, long expiresAtSeconds) {
        try {
            client.eval(
                    "local jti, uid, tp, exp = ...; " +
                            "box.space.tokens:insert({jti, uid, tp, exp, false})",
                    Arrays.asList(jti, userId.longValue(), tokenType, expiresAtSeconds)
            ).get();
        } catch (Exception e) {
            log.error("Ошибка при сохранении токена", e);
        }
    }

    public boolean isValid(String jti) {
        try {
            var response = client.eval(
                    "local jti = ...; " +
                            "local t = box.space.tokens:get({jti}); " +
                            "if t == nil then return false end; " +
                            "if t[5] then return false end; " +
                            "if t[4] < os.time() then return false end; " +
                            "return true",
                    List.of(jti)
            ).get();

            var dataList = (List<?>) response.get();

            if (dataList == null || dataList.isEmpty()) {
                return false;
            }
            return Boolean.TRUE.equals(dataList.get(0));
        } catch (Exception e) {
            log.error("Ошибка при проверке токена", e);
            return false;
        }
    }

    public void revokeToken(String jti) {
        try {
            client.eval(
                    "local jti = ...; box.space.tokens:update({jti}, {{'=', 5, true}})",
                    List.of(jti)
            ).get();
        } catch (Exception e) {
            log.error("Ошибка при отзыве токена", e);
        }
    }

    public void revokeAllUserTokens(Integer userId, String tokenType) {
        try {
            client.eval(
                    "local uid, tp = ...; " +
                            "for _, t in box.space.tokens.index.by_user:pairs({uid, tp}) do " +
                            "  box.space.tokens:update(t[1], {{'=', 5, true}}) " +
                            "end",
                    Arrays.asList(userId.longValue(), tokenType)
            ).get();
        } catch (Exception e) {
            log.error("Ошибка при отзыве всех токенов юзера", e);
        }
    }

    public String saveOtp(String email, int code, String otpType, long expiresAtSeconds) {
        String id = UUID.randomUUID().toString();
        try {
            client.eval(
                    "local id, em, code, tp, exp = ...; " +
                            "box.space.otps:insert({id, em, code, tp, exp, false})",
                    Arrays.asList(id, email, (long) code, otpType, expiresAtSeconds)
            ).get();
            return id;
        } catch (Exception e) {
            log.error("Ошибка сохранения OTP", e);
            throw new RuntimeException(e);
        }
    }

    public boolean verifyOtp(String email, int code, String otpType) {
        try {
            var response = client.eval(
                    "local em, code, tp = ...; " +
                            "for _, t in box.space.otps.index.by_email:pairs({em, tp}) do " +
                            "  if t[3] == code and not t[6] and t[5] >= os.time() then " +
                            "    box.space.otps:update(t[1], {{'=', 6, true}}) " +
                            "    return true " +
                            "  end " +
                            "end; " +
                            "return false",
                    Arrays.asList(email, (long) code, otpType)
            ).get();

            var dataList = (List<?>) response.get();

            if (dataList == null || dataList.isEmpty()) {
                return false;
            }
            return Boolean.TRUE.equals(dataList.get(0));
        } catch (Exception e) {
            log.error("Ошибка проверки OTP", e);
            return false;
        }
    }
}