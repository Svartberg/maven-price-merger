package com.svartberg.service;

import com.svartberg.model.Price;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MergePriceImplTest {

  private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
  PriceMergerImpl priceMerger = new PriceMergerImpl();
  private LinkedList<Price> result;

  private Price mockPrice(Date begin, Date end, long val){
    return mockPrice(1, begin, end, val);
  }
  private Price mockPrice(int dep, Date begin, Date end, long val){
    return mockPrice(1, dep, begin, end, val);
  }
  private Price mockPrice(int number, int dep, Date begin, Date end, long val){
    return mockPrice("122856", number, dep, begin, end, val);
  }
  private Price mockPrice(String code, int number, int dep, Date begin, Date end, long val){
    return Price.builder()
        .id(1L)
        .productCode(code)
        .number(number)
        .depart(dep)
        .begin(begin)
        .end(end)
        .value(val)
        .build();
  }

  // old       |---| |---|
  // new |---|
  // res |---| |---| |---|
  @Test
  public void mergeTwoPrice_left() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
      var newPrices = new ArrayList<>(Arrays.asList(
          mockPrice(sdf.parse("01.01.2013 00:00:00"), sdf.parse("09.01.2013 00:00:00") , 12000)
      ));
      var expected = new ArrayList<>(Arrays.asList(
          mockPrice(sdf.parse("01.01.2013 00:00:00"), sdf.parse("09.01.2013 00:00:00") , 12000),
          mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
          mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
      ));
      List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old    |---| |---|
  // new |---|
  // res |---||-| |---|
  @Test
  public void mergeTwoPrice_left_intersect() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("01.01.2013 00:00:00"), sdf.parse("12.01.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("01.01.2013 00:00:00"), sdf.parse("12.01.2013 00:00:00") , 12000),
        mockPrice(sdf.parse("12.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |-----| |---|
  // new |-|
  // res |-||--| |---|
  @Test
  public void mergeTwoPrice_left_intersect2() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("15.01.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("15.01.2013 00:00:00") , 12000),
        mockPrice(sdf.parse("15.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |-------| |---|
  // new    |-|
  // res |-||-||-| |---|
  @Test
  public void mergeTwoPrice_split() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("12.01.2013 00:00:00"), sdf.parse("15.01.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("12.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("12.01.2013 00:00:00"), sdf.parse("15.01.2013 00:00:00") , 12000),
        mockPrice(sdf.parse("15.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |----| |---|
  // new    |-|
  // res |-||-| |---|
  @Test
  public void mergeTwoPrice_break_left() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("20.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("20.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("20.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 12000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |----||-----|
  // new    |----|
  // res |-||----||--|
  @Test
  public void mergeTwoPrice_overlap() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("25.01.2013 00:00:00"), sdf.parse("10.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("25.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("25.01.2013 00:00:00"), sdf.parse("10.02.2013 00:00:00") , 12000),
            mockPrice(sdf.parse("10.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |----||-----|
  // new       |-|
  // res |----||-||--|
  @Test
  public void mergeTwoPrice_break_second() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("05.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("05.02.2013 00:00:00") , 12000),
            mockPrice(sdf.parse("05.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |----||-------|
  // new          |-|
  // res |----||-||-||-|
  @Test
  public void mergeTwoPrice_split_second() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("31.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("10.02.2013 00:00:00") , 99000),
            mockPrice(sdf.parse("10.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000),
            mockPrice(sdf.parse("20.02.2013 00:00:00"), sdf.parse("31.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |-5--||-9---|
  // new |-7--|
  // res |-7--||-9---|
  // 5, 7 and 9 - values
  @Test
  public void mergeTwoPrice_replace_first() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 12000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |-5--||-9---|
  // new       |-7---|
  // res |-5--||-7---|
  // 5, 7 and 9 - values
  @Test
  public void mergeTwoPrice_replace_second() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |----||-----|
  // new              |-|
  // res |----||-----||-|
  @Test
  public void mergeTwoPrice_add_after() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("15.02.2013 00:00:00"), sdf.parse("30.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000),
            mockPrice(sdf.parse("15.02.2013 00:00:00"), sdf.parse("30.02.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old   |----||-----|
  // new |================|
  // res |================|
  @Test
  public void mergeTwoPrice_overlap_all() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("01.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("28.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("01.12.2012 00:00:00"), sdf.parse("30.03.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(sdf.parse("01.12.2012 00:00:00"), sdf.parse("30.03.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  // old |-5--||-9---|
  // new    |-5--|
  // res |-5-----||9-|
  // 5 and 9 - values
  @Test
  public void mergeTwoPrice_update_dates_and_shift() throws ParseException {
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("20.01.2013 00:00:00"), sdf.parse("10.02.2013 00:00:00") , 11000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
        mockPrice(sdf.parse("10.01.2013 00:00:00"), sdf.parse("10.02.2013 00:00:00") , 11000),
        mockPrice(sdf.parse("10.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  @Test
  public void update_for_number() throws ParseException{
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(1, 1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(2, 1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(2,1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(2,1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  @Test
  public void update_for_department() throws ParseException{
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(2, sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
        mockPrice(2, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
        mockPrice(1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(2, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  @Test
  public void update_for_product() throws ParseException{
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice("122856", 1, 1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice("6654", 1, 1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("15.02.2013 00:00:00") , 99000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice("6654", 1,1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice("6654", 1, 1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000),
            mockPrice("122856", 1, 1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  @Test
  public void new_for_number() throws ParseException{
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice(1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice(2,1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice(1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
            mockPrice(2,1, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  @Test
  public void new_for_department() throws ParseException{
    var oldPrices = new ArrayList<>(Arrays.asList(
        mockPrice(1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
        mockPrice(2, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
        mockPrice(1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000),
        mockPrice(2, sdf.parse("01.02.2013 00:00:00"), sdf.parse("20.02.2013 00:00:00") , 12000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);
  }

  @Test
  public void new_for_product() throws ParseException{
    var oldPrices = new ArrayList<>(Arrays.asList(
            mockPrice("122856", 1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000)
    ));
    var newPrices = new ArrayList<>(Arrays.asList(
            mockPrice("6654", 1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 12000)
    ));
    var expected = new ArrayList<>(Arrays.asList(
            mockPrice("6654", 1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 12000),
            mockPrice("122856", 1,1, sdf.parse("10.01.2013 00:00:00"), sdf.parse("31.01.2013 00:00:00") , 11000)
    ));
    List<Price> result = priceMerger.merge(oldPrices, newPrices);
    Assertions.assertIterableEquals(expected,result);

  }
}