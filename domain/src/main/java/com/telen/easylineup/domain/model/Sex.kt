package com.telen.easylineup.domain.model

enum class Sex(val id: Int) {
    MALE(1),
    FEMALE(2);

    companion object {
        fun getById(id: Int): Sex? {
            return values().firstOrNull { it.id == id }
        }
    }
}