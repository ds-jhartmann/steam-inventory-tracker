import {Component, Input, OnInit} from '@angular/core';
import {Item} from "../items/item";
import {ActivatedRoute} from "@angular/router";
import {ItemService} from "../item.service";
import {Location} from "@angular/common";
import {PriceHistory} from "../items/priceHistory";
import {PriceTrend} from "../items/priceTrend";

@Component({
  selector: 'app-item-detail',
  templateUrl: './item-detail.component.html',
  styleUrls: ['./item-detail.component.css']
})
export class ItemDetailComponent implements OnInit {
  @Input() item?: Item;
  @Input() priceHistory?: PriceHistory[];
  @Input() priceTrend1?: PriceTrend;
  @Input() priceTrend7?: PriceTrend;
  @Input() priceTrend30?: PriceTrend;

  constructor(
    private route: ActivatedRoute,
    private itemService: ItemService,
    private location: Location
  ) {
  }

  ngOnInit(): void {
    this.getItem();
    this.getPriceHistory();
    this.getPriceTrend1Day();
    this.getPriceTrend7Day();
    this.getPriceTrend30Day();
  }

  goBack(): void {
    this.location.back();
  }

  private getItem(): void {
    const name = this.route.snapshot.paramMap.get('itemName')!;
    this.itemService.getItem(name).subscribe(item => this.item = item);
  }

  private getPriceHistory(): void {
    const name = this.route.snapshot.paramMap.get('itemName')!;
    this.itemService.getPriceHistory(name).subscribe(priceHistory => {
      this.priceHistory = priceHistory
      priceHistory.sort((a, b) => {
        const timestamp1 = a.timestamp;
        const timestamp2 = b.timestamp;
        if (timestamp2 > timestamp1) return -1;
        if (timestamp2 < timestamp1) return 1;
        return 0;
      });
    });
  }

  private getPriceTrend1Day(): void {
    const name = this.route.snapshot.paramMap.get('itemName')!;
    this.itemService.getPriceTrend(name, 1, "DAYS").subscribe(priceTrend => this.priceTrend1 = priceTrend);
  }

  private getPriceTrend7Day(): void {
    const name = this.route.snapshot.paramMap.get('itemName')!;
    this.itemService.getPriceTrend(name, 7, "DAYS").subscribe(priceTrend => this.priceTrend7 = priceTrend);
  }

  private getPriceTrend30Day(): void {
    const name = this.route.snapshot.paramMap.get('itemName')!;
    this.itemService.getPriceTrend(name, 30, "DAYS").subscribe(priceTrend => this.priceTrend30 = priceTrend);
  }
}
