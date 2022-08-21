package com.svartberg.service;

import com.svartberg.model.Price;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PriceMergerImpl implements PriceMerger {

  @Override
  public List<Price> merge(List<Price> currentPrices, List<Price> newPrices) {

    if (newPrices == null || newPrices.isEmpty()) {
      return currentPrices;
    }
    if (currentPrices == null || currentPrices.isEmpty()) {
      return newPrices;
    }
    Map<String, Map<Integer, Map<Integer, List<Price>>>> oldPriceMap = currentPrices.stream()
        .collect(Collectors.groupingBy(Price::getProductCode,
            Collectors.groupingBy(Price::getDepart, Collectors.groupingBy(Price::getNumber))));

    Map<String, Map<Integer, Map<Integer, List<Price>>>> newPriceMap = newPrices.stream().collect(
        Collectors.groupingBy(Price::getProductCode,
            Collectors.groupingBy(Price::getDepart, Collectors.groupingBy(Price::getNumber))));

    //for each product
    newPriceMap.forEach((k, v) -> {
      if (!oldPriceMap.containsKey(k)) {
        oldPriceMap.put(k, v);
      } else {
        mergeWithinProduct(oldPriceMap.get(k), v);
      }
    });

    //flatten map of maps
    return oldPriceMap.values().stream().flatMap(
            o -> o.values().stream().flatMap(o2 -> o2.values().stream().flatMap(Collection::stream)))
            .collect(Collectors.toList());
  }

  private void mergeWithinProduct(Map<Integer, Map<Integer, List<Price>>> oldProductPrices,
      Map<Integer, Map<Integer, List<Price>>> newProductPrices) {
    //for each department
    newProductPrices.forEach((k, v) -> {
      if (!oldProductPrices.containsKey(k)) {
        oldProductPrices.put(k, v);
      } else {
        mergePriceWithinDepartment(oldProductPrices.get(k), v);
      }
    });
  }

  private void mergePriceWithinDepartment(Map<Integer, List<Price>> oldProdInDepartmentPrice,
      Map<Integer, List<Price>> newProdInDepPrice) {
    newProdInDepPrice.forEach((k, v) -> {
      if (!oldProdInDepartmentPrice.containsKey(k)) {
        oldProdInDepartmentPrice.put(k, v);
      } else {
        mergePricesWithinGroup(oldProdInDepartmentPrice.get(k), v);
      }
    });
  }

  //using 2 iterators we will slide through all lists (sorted by begin date) and merge
  private void mergePricesWithinGroup(List<Price> oldPrices, List<Price> newPrices) {
    //here we have prices from the same product, same department, same number(group)
    //put prices in a stack. Stack represents timeline
    LinkedList<Price> stack = new LinkedList<>();
    oldPrices.sort(Comparator.comparing(Price::getBegin));
    newPrices.sort(Comparator.comparing(Price::getBegin));
    int opi = 0; //index for old price
    int npi = 0; //index for new price
    while (opi < oldPrices.size() && npi < newPrices.size()) {
      var oldPrice = oldPrices.get(opi);
      var newPrice = newPrices.get(npi);
      var prevInStackPrice = stack.peekLast();
      if (prevInStackPrice != null && newPrice.getBegin().before(prevInStackPrice.getBegin())) {
        prevInStackPrice.setEnd(newPrice.getBegin());
      }
      if (newPrice.getEnd().before(oldPrice.getBegin()))//new price is before all old prices we add it
      {
        stack.add(newPrice);
        npi++;
        continue;
      }
      if (oldPrice.getEnd().before(newPrice.getBegin()))//old price is before all old prices we add it
      {
        stack.add(oldPrice);
        opi++;
        continue;
      }
      //new price overlaps
      if ((newPrice.getBegin().equals(oldPrice.getBegin()) || newPrice.getBegin().before(oldPrice.getBegin())) && (
          newPrice.getEnd().after(oldPrice.getEnd()) || newPrice.getEnd().equals(oldPrice.getEnd()))) {
        stack.add(newPrice);
        opi++;
        npi++;
        continue;
      }
      //if intersection
      if (newPrice.getEnd().before(oldPrice.getEnd()) || newPrice.getBegin().after(oldPrice.getBegin()) || (
          newPrice.getBegin().before(oldPrice.getBegin()) && newPrice.getEnd().equals(oldPrice.getEnd())) || (
          newPrice.getBegin().equals(oldPrice.getBegin()) && newPrice.getEnd().after(oldPrice.getEnd()))) {
        if (oldPrice.getValue() == newPrice.getValue()) { //new price increase
          adjustTimeOfOldPriceWithinNewPrice(oldPrice, newPrice);
          stack.add(oldPrice);
          opi++;
          npi++;
        } else {
          //split
          if (newPrice.getEnd().before(oldPrice.getEnd()) && newPrice.getBegin().after(oldPrice.getBegin())) {
            var opSplit = new Price(oldPrice);
            oldPrice.setEnd(newPrice.getBegin());
            stack.add(oldPrice);
            stack.add(newPrice);
            opSplit.setBegin(newPrice.getEnd());
            stack.add(opSplit);
            opi++;
            npi++;
          } else {
            //break
            if (newPrice.getBegin().before(oldPrice.getBegin()) || newPrice.getBegin().equals(oldPrice.getBegin())) {
              //left
              stack.add(newPrice);
              oldPrice.setBegin(newPrice.getEnd());
              stack.add(oldPrice);
              npi++;
              opi++;
            } else {
              //right
              oldPrice.setEnd(newPrice.getBegin());
              stack.add(oldPrice);
              stack.add(newPrice);
              npi++;
              opi++;
            }
          }
        }
      }
    }
    addRemanindOldPrices(oldPrices, stack, opi);
    addRemaningNewPrices(newPrices, stack, npi);
    oldPrices.clear();
    oldPrices.addAll(stack);
  }

  private void adjustTimeOfOldPriceWithinNewPrice(Price op, Price np) {
    if (np.getEnd().after(op.getEnd())) {
      op.setEnd(np.getEnd());
    }
    if (np.getBegin().before(op.getBegin())) {
      op.setBegin(np.getBegin());
    }
  }

  private void addRemaningNewPrices(List<Price> newPrices, LinkedList<Price> stack, int npi) {
    while (npi < newPrices.size()) {
      stack.add(newPrices.get(npi));
      npi++;
    }
  }

  private void addRemanindOldPrices(List<Price> oldPrices, LinkedList<Price> stack, int opi) {
    while (opi < oldPrices.size()) {
      var last = stack.peekLast();
      if (last != null && last.getEnd().after(oldPrices.get(opi).getEnd())) {
        opi++;
        continue;
      }
      if (last != null && last.getEnd().after(oldPrices.get(opi).getBegin())) {
        oldPrices.get(opi).setBegin(last.getEnd());
        stack.add(oldPrices.get(opi));
        opi++;
        continue;
      }
      stack.add(oldPrices.get(opi));
      opi++;
    }
  }
}
