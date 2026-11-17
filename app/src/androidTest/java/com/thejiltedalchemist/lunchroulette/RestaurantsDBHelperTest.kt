package com.thejiltedalchemist.lunchroulette

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Uses a throwaway database file so the app's real data is never touched. */
@RunWith(AndroidJUnit4::class)
class RestaurantsDBHelperTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var helper: RestaurantsDBHelper

    @Before
    fun setUp() {
        context.deleteDatabase(TEST_DB)
        helper = RestaurantsDBHelper(context, TEST_DB)
    }

    @After
    fun tearDown() {
        helper.close()
        context.deleteDatabase(TEST_DB)
    }

    private fun names() = helper.readAllRestaurants().map { it.name }

    private fun insert(vararg names: String) =
        names.forEach { assertEquals(InsertResult.INSERTED, helper.insertRestaurant(RestaurantsModel(it))) }

    @Test
    fun exactDuplicateIsRejected() {
        insert("Pizza")
        assertEquals(InsertResult.DUPLICATE, helper.insertRestaurant(RestaurantsModel("Pizza")))
        assertEquals(listOf("Pizza"), names())
    }

    @Test
    fun caseVariantIsRejected() {
        insert("Pizza")
        assertEquals(InsertResult.DUPLICATE, helper.insertRestaurant(RestaurantsModel("pizza")))
        assertEquals(InsertResult.DUPLICATE, helper.insertRestaurant(RestaurantsModel("PIZZA")))
        assertEquals(listOf("Pizza"), names())
    }

    @Test
    fun insertKeepsInsertionOrder() {
        insert("C", "A", "B")
        assertEquals(listOf("C", "A", "B"), names())
    }

    @Test
    fun deleteMatchesNameLiterally_notAsLikePattern() {
        insert("abc", "a_c", "100%")
        assertTrue(helper.deleteRestaurant("a_c"))
        assertEquals(listOf("abc", "100%"), names())
        assertFalse(helper.deleteRestaurant("%"))
        assertEquals(listOf("abc", "100%"), names())
    }

    @Test
    fun deleteOfAbsentNameReturnsFalse() {
        insert("Pizza")
        assertFalse(helper.deleteRestaurant("Sushi"))
        assertEquals(listOf("Pizza"), names())
    }

    @Test
    fun insertAtPositionKeepsStoredOrderInSyncWithTheList() {
        insert("A", "B", "C")
        assertEquals(InsertResult.INSERTED, helper.insertRestaurantAt(0, RestaurantsModel("X")))
        assertEquals(InsertResult.INSERTED, helper.insertRestaurantAt(2, RestaurantsModel("Y")))
        assertEquals(InsertResult.INSERTED, helper.insertRestaurantAt(99, RestaurantsModel("Z")))
        assertEquals(listOf("X", "A", "Y", "B", "C", "Z"), names())
    }

    @Test
    fun insertAtPositionRejectsDuplicateAndLeavesTableUnchanged() {
        insert("A", "B", "C")
        assertEquals(InsertResult.DUPLICATE, helper.insertRestaurantAt(1, RestaurantsModel("b")))
        assertEquals(listOf("A", "B", "C"), names())
    }

    @Test
    fun migratesVersion1DatabaseKeepingOrderAndDroppingCaseDuplicates() {
        helper.close()
        context.deleteDatabase(TEST_DB)

        // Build a v1 database exactly as the old app did: case-sensitive primary key, user_version 1.
        val path = context.getDatabasePath(TEST_DB)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { db ->
            db.execSQL("CREATE TABLE restaurants (name TEXT PRIMARY KEY, address TEXT)")
            listOf("Pizza", "Taco", "pizza", "Sushi").forEach { db.execSQL("INSERT INTO restaurants (name) VALUES (?)", arrayOf(it)) }
            db.version = 1
        }

        helper = RestaurantsDBHelper(context, TEST_DB)
        assertEquals(listOf("Pizza", "Taco", "Sushi"), names())
        assertEquals(InsertResult.DUPLICATE, helper.insertRestaurant(RestaurantsModel("TACO")))
    }

    private companion object {
        const val TEST_DB = "restaurants_test.db"
    }
}
