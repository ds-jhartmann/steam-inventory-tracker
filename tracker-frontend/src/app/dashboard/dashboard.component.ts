import {Component, OnInit} from '@angular/core';
import {Item} from "../items/item";
import {ItemService} from "../item.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  items: Item[] = [];

  constructor(private itemService: ItemService) {
  }

  ngOnInit(): void {
    this.getItems();
  }

  getItems(): void {
    this.itemService.getItems()
      .subscribe(items => {
        items.sort((a, b) => {
          const price1 = a.priceHistory.at(a.priceHistory.length - 1)!.price;
          const price2 = b.priceHistory.at(b.priceHistory.length - 1)!.price;
          if (price2 > price1) return 1;
          if (price2 < price1) return -1;
          return 0;
        });
        this.items = items.slice(1, 5)
      });
  }
}
