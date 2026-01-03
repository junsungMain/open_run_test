package kr.co.miracle.open_run_test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import org.assertj.core.api.Assertions.assertThat

@SpringBootTest
class CouponServiceTest @Autowired constructor(
    private val redissonLockFacade: RedissonLockFacade,
    private val couponRepository: CouponRepository
) {
    private var couponId: Long = 0L

    @BeforeEach
    fun setUp() {
        // 쿠폰 생성 및 저장 -> 저장된 객체를 반환받음
        val savedCoupon = couponRepository.save(Coupon(name = "선착순 치킨", availableStock = 100))

        // ★ 중요: DB가 만들어준 진짜 ID를 변수에 저장
        couponId = savedCoupon.id ?: throw IllegalStateException("ID 생성 실패")
    }

    @Test
    fun `쿠폰 100개를 동시에 100명이 요청하면 재고는 0이 되어야 한다`() {
        // given
        val threadCount = 100
        // 멀티스레드 환경을 만들기 위한 ExecutorService (비동기 작업 수행)
        val executorService = Executors.newFixedThreadPool(32)
        // 100개의 요청이 다 끝날 때까지 기다리기 위한 장치
        val latch = CountDownLatch(threadCount)

        // when
        repeat(threadCount) {
            executorService.submit {
                try {
                    redissonLockFacade.issueCoupon(couponId) // 1번 쿠폰 발급 시도
                } finally {
                    latch.countDown() // 작업 완료 시 카운트 감소
                }
            }
        }

        latch.await() // 모든 스레드가 끝날 때까지 대기

        // then
        val coupon = couponRepository.findById(couponId).get()
        println("남은 쿠폰 수량: ${coupon.availableStock}")

        // 기대값: 0, 실제값: 아마도 0이 아닐 것임 (예: 89, 75 등)
        assertThat(coupon.availableStock).isEqualTo(0)
    }
}