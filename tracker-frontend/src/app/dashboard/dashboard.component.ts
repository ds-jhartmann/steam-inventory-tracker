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
      .subscribe(items => this.items = items.slice(1, 5));
  }
}
