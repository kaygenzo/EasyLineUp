/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.domain

import com.telen.easylineup.domain.model.matchesSearch
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SearchTextTests {
    @Test
    fun aBlankQueryMatchesEverything() {
        assertTrue("Fall Classic".matchesSearch(""))
        assertTrue("Fall Classic".matchesSearch("  "))
    }

    @Test
    fun ignoresTheCaseBeyondAscii() {
        assertTrue("Été".matchesSearch("été"))
        assertTrue("ÖRNEK".matchesSearch("örnek"))
        assertTrue("Ελλάδα".matchesSearch("ΕΛΛΆΔΑ"))
    }

    @Test
    fun ignoresTheAccents() {
        assertTrue("Tournoi d'Été".matchesSearch("ete"))
        assertTrue("Ete".matchesSearch("été"))
        assertTrue("İstanbul Kupası".matchesSearch("istanbul"))
    }

    @Test
    fun ignoresSpacesAroundTheQuery() {
        assertTrue("Fall Classic".matchesSearch(" fall "))
    }

    @Test
    fun takesEveryCharacterLiterally() {
        assertTrue("100% effort".matchesSearch("100%"))
        assertFalse("1000 runs".matchesSearch("100%"))
        assertFalse("U312 final".matchesSearch("U_1"))
    }
}
