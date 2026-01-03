package kr.co.miracle.open_run_test

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponRepository: CouponRepository
) {

    @Transactional // 트랜잭션 시작
    fun issueCoupon(couponId: Long) {
        val coupon = couponRepository.findById(couponId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 쿠폰입니다.") }

        coupon.decreaseStock()
    }
}