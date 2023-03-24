import {Component, Input, OnInit} from '@angular/core';
import {Item} from "../items/item";
import {ActivatedRoute} from "@angular/router";
import {ItemService} from "../item.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-item-detail',
  templateUrl: './item-detail.component.html',
  styleUrls: ['./item-detail.component.css']
})
export class ItemDetailComponent implements OnInit {
  @Input() item?: Item;

  constructor(
    private route: ActivatedRoute,
    private itemService: ItemService,
    private location: Location
  ) {
  }

  ngOnInit(): void {
    this.getItem();
  }

  private getItem(): void {
    const name = this.route.snapshot.paramMap.get('itemName')!;
    this.itemService.getItem(name).subscribe(item => this.item = item);
  }

  goBack():void {
    this.location.back();
  }
}
