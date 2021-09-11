package com.mitsuki.armory.adapter.empty

sealed class EmptyState(val isEmpty: Boolean) {

    class Normal(isEmpty: Boolean, val hint: String = "") : EmptyState(isEmpty) {
        override fun equals(other: Any?): Boolean {
            return other is Normal && isEmpty == other.isEmpty && hint == other.hint
        }

        override fun hashCode(): Int {
            return isEmpty.hashCode() + hint.hashCode()
        }
    }

    class Error(val error: Throwable) : EmptyState(true) {
        override fun equals(other: Any?): Boolean {
            return other is Error &&
                    isEmpty == other.isEmpty &&
                    error == other.error
        }

        override fun hashCode(): Int {
            return isEmpty.hashCode() + error.hashCode()
        }
    }
}