package com.shoppingbasket.util

import com.shoppingbasket.api.ProductCatalog.Product

object SampleProducts {
  val pencil = Product("1", "pencil", "Superior extra-refined, high-density graphite with HB hardness", 1.75)
  val smartPencil = Product("2", "smart pencil",
    s"""Featuring the 2.6mm fine tip, PenPower Pencil provides a smooth
       |and nature writing experience on smartphone and tablet. You can freely write or sketch with PenPower Pencil on
       |any capacitive touch panel devices. With a triple-A battery, PenPower Pencil can work continuously for 12 hours.
       |For the sake of saving battery power, the PenPower pencil will be automatically turned off after 3 minutes of
       |inactivity.""".stripMargin, 39.99)
}
