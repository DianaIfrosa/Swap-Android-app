package com.diana.bachelorthesis.view

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.PositionAssertions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import com.diana.bachelorthesis.R
import junit.framework.TestCase
import org.hamcrest.Matchers.*
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class AddItemFragmentTest
    : TestCase()
{
    private lateinit var scenario: FragmentScenario<AddItemFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_BachelorThesis)
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun mandatoryFieldsAreRequired() {
        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        onView(withId(R.id.item_title)).perform(scrollTo())

        // verify the mandatory fields status appears with the 'Required' message
        onView(withId(R.id.item_title_status)).check(matches(isDisplayed()))
        onView(withId(R.id.item_title_status)).check(isCompletelyRightOf(withId(R.id.item_title)))
        onView(withId(R.id.item_title_status)).check(matches(withText(R.string.required)))

        onView(withId(R.id.item_description)).check(matches(isDisplayed()))
        onView(withId(R.id.item_description_status)).check(isCompletelyRightOf(withId(R.id.item_description)))
        onView(withId(R.id.item_description_status)).check(matches(withText(R.string.required)))

        onView(withId(R.id.item_purpose)).check(matches(isDisplayed()))
        onView(withId(R.id.item_purpose_status)).check(isCompletelyRightOf(withId(R.id.item_purpose)))
        onView(withId(R.id.item_purpose_status)).check(matches(withText(R.string.required)))

        onView(withId(R.id.item_category)).perform(scrollTo())
        onView(withId(R.id.item_category)).check(matches(isDisplayed()))
        onView(withId(R.id.item_category_status)).check(isCompletelyRightOf(withId(R.id.item_category)))
        onView(withId(R.id.item_category_status)).check(matches(withText(R.string.required)))

        onView(withId(R.id.item_photos)).perform(scrollTo())
        onView(withId(R.id.item_photos)).check(matches(isDisplayed()))
        onView(withId(R.id.item_photos_status)).check(isCompletelyRightOf(withId(R.id.item_photos)))
        onView(withId(R.id.item_photos_status)).check(matches(withText(R.string.required)))
    }

    @Test
    fun titleLessCharacters() {
        onView(withId(R.id.item_title_edittext)).perform(typeText("Ti"))

        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        // verify the title status field appears with the message 'At least 3 characters required'
        onView(withId(R.id.item_title)).perform(scrollTo())
        onView(withId(R.id.item_title_status)).check(matches(isDisplayed()))
        onView(withId(R.id.item_title_status)).check(matches(withText(R.string.item_title_short)))
    }

    @Test
    fun descriptionLessCharacters() {
        onView(withId(R.id.item_description_edittext)).perform(typeText("Desc"))

        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        // verify the title status field appears with the message 'At least 5 characters required'
        onView(withId(R.id.item_description)).perform(scrollTo())
        onView(withId(R.id.item_description_status)).check(matches(isDisplayed()))
        onView(withId(R.id.item_description_status)).check(matches(withText(R.string.item_description_short)))
    }

    @Test
    fun titleMinimumCharacters() {
        // type a 3 characters title into the designated field
        onView(withId(R.id.item_title_edittext)).perform(typeText("Hey"))

        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        // verify the title status field does not have any message
        onView(withId(R.id.item_title)).perform(scrollTo())
        onView(withId(R.id.item_title_status)).check(matches(not(isDisplayed())))
    }

    @Test
    fun descriptionMinimumCharacters() {
        // type a 5 characters description into the designated field
        onView(withId(R.id.item_description_edittext)).perform(typeText("Descr"))

        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        // verify the description status field does not have any message
        onView(withId(R.id.item_description)).perform(scrollTo())
        onView(withId(R.id.item_description_status)).check(matches(not(isDisplayed())))
    }

    @Test
    fun titleManyCharacters() {
        // type more than 3 characters into the title designated field
        onView(withId(R.id.item_title_edittext)).perform(typeText("Title long enough"))

        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        // verify the title status field does not have any message
        onView(withId(R.id.item_title)).perform(scrollTo())
        onView(withId(R.id.item_title_status)).check(matches(not(isDisplayed())))
    }

    @Test
    fun descriptionManyCharacters() {
        // type more than 5 characters into the description designated field
        onView(withId(R.id.item_description_edittext)).perform(typeText("Description long enough"))

        // click the Save button and wait for the fields validation to be finished
        onView(withId(R.id.save_item)).perform(scrollTo(),click())
        Thread.sleep(1500)

        // verify the description status field does not have any message
        onView(withId(R.id.item_description)).perform(scrollTo())
        onView(withId(R.id.item_description_status)).check(matches(not(isDisplayed())))
    }

    @Test
    fun categorySelection() {
        val category = "Education"

        onView(withId(R.id.item_category)).perform(scrollTo())
        onView(withId(R.id.spinner_categories)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`(category))).perform(click())

        // verify the category chosen is displayed
        onView(withId(R.id.spinner_categories)).check(matches(withSpinnerText(containsString(category))))
        // verify the category status field does not have any message
        onView(withId(R.id.item_category_status)).check(matches(not(isDisplayed())))
    }

    @Test
    fun exchangePurposeTriggersSectionExpansion() {
       checkExchangePurpose()
    }

    @Test
    fun donationPurposeDoesNotTriggerSectionExpansion() {
        checkDonationPurpose()
    }

    @Test
    fun alternateBetweenExchangeAndDonate() {
        checkDonationPurpose()
        checkExchangePurpose()
        checkDonationPurpose()
        checkExchangePurpose()
    }

    @Test
    fun selectMultipleTimesExchangePurpose() {
        // check that for multiple selections of 'Exchange' button the section designated to Exchange
        // does appear, and that the button remains checked
        for (i in 1..4) {
           checkExchangePurpose()
        }
    }

    @Test
    fun selectMultipleTimesDonationPurpose() {
        // check that for multiple selections of 'Donate' button the section designated to Exchange
        // does not appear, and that the button remains checked
        for (i in 1..4) {
           checkDonationPurpose()
        }
    }

    @Test
    fun openOtherDetails() {
        onView(withId(R.id.text_other_details)).perform(scrollTo())
        onView(withId(R.id.text_other_details)).perform(click())
        onView(withId(R.id.save_item)).perform(scrollTo())

        Thread.sleep(1000)

        // check hidden layout appears below the text and the drawable icon is changed into arrow up
        onView(withId(R.id.hidden_layout_other_details)).check(matches(isDisplayed()))
        onView(withId(R.id.hidden_layout_other_details)).check(isCompletelyBelow(withId(R.id.text_other_details)))
        onView(withId(R.id.text_other_details)).check(matches(withTagValue(equalTo(R.drawable.ic_arrow_dropup))))

        // check that after clicking again on the icon, the hidden layout disappears and
        // the drawable icon is changed into arrow down
        onView(withId(R.id.text_other_details)).perform(click())
        onView(withId(R.id.hidden_layout_other_details)).check(matches(not(isDisplayed())))
        onView(withId(R.id.text_other_details)).check(matches(withTagValue(equalTo(R.drawable.ic_arrow_dropdown))))
    }

    fun checkExchangePurpose() {
        onView(allOf(withId(R.id.radioButtonExchange), withText(R.string.exchange))).perform(click())

        onView(allOf(withId(R.id.radioButtonExchange), withText(R.string.exchange))).check(matches(
            isChecked()
        ))
        onView(withId(R.id.layout_preferences)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_preferences)).check(isCompletelyBelow(withId(R.id.radioButtonExchange)))
    }

    fun checkDonationPurpose() {
        onView(allOf(withId(R.id.radioButtonDonate), withText(R.string.donate))).perform(click())

        onView(allOf(withId(R.id.radioButtonDonate), withText(R.string.donate))).check(matches(
            isChecked()
        ))

        onView(withId(R.id.layout_preferences)).check(matches(not(isDisplayed())))
    }

    //TODO pus strings in documentatie

}