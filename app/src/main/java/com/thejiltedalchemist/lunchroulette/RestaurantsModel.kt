package com.thejiltedalchemist.lunchroulette

data class RestaurantsModel(val name: String)

private val WHITESPACE_RUNS = Regex("\\s+")

/**
 * Canonical form of a user-entered restaurant name: trimmed, with every run of whitespace collapsed
 * to a single space, so "Pizza  Hut " and "Pizza Hut" are the same name. Case is preserved for display;
 * the database column compares names case-insensitively.
 */
fun normalizeRestaurantName(raw: String): String = raw.trim().replace(WHITESPACE_RUNS, " ")
