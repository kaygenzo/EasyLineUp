package com.telen.easylineup.repository.model

enum class TeamType(val id: Int, val position: Int) {
    UNKNOWN(0, -1),
    BASEBALL(1, 0),
    SOFTBALL(2, 1);

    companion object {
        fun getTypeById(id: Int): TeamType {
            values().forEach {
                if(it.id == id)
                    return it
            }
            return UNKNOWN
        }
    }
}