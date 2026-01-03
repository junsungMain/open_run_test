package kr.co.miracle.open_run_test

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedissonLockFacade(
    private val redissonClient: RedissonClient,
    private val couponService: CouponService // 기존 서비스 주입
) {

    fun issueCoupon(couponId: Long) {
        val lock = redissonClient.getLock("coupon_lock:$couponId")

        try {
            val available = lock.tryLock(10, TimeUnit.SECONDS)

            if (!available) {
                println("락 획득 실패 (사람이 너무 많음)")
                return
            }

            couponService.issueCoupon(couponId)

        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            lock.unlock()
        }
    }
}