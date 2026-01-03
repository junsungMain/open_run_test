package kr.co.miracle.open_run_test

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Coupon(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    var availableStock: Int // 남은 수량
) {
    fun decreaseStock() {
        if (availableStock <= 0) {
            throw RuntimeException("재고가 모두 소진되었습니다.")
        }
        availableStock--
    }
}