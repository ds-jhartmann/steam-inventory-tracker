import {Component, OnInit} from '@angular/core';
import {ItemService} from "../item.service";
import {MessageService} from "../message.service";
import {PriceTrendItem} from "../items/PriceTrendItem";
import {forkJoin, map} from "rxjs";

enum ChronoUnits {
  DAYS = "DAYS",
  WEEKS = "WEEKS",
  MONTHS = "MONTHS",
  YEARS = "YEARS",
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  priceTrendItems: PriceTrendItem[] = [];
  priceTrendItemsAbsolute: PriceTrendItem[] = [];
  timeSpan: ChronoUnits = ChronoUnits.DAYS;
  constructor(private itemService: ItemService, private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.timeSpan = ChronoUnits.DAYS;
    this.getItems(ChronoUnits.DAYS);
  }

  priceTrend(timeSpan: ChronoUnits) {
    this.timeSpan = timeSpan;
      this.getItems(timeSpan);
  }

  getItems(timeSpan: ChronoUnits): void {
    this.itemService.getItems()
      .subscribe(items => {
        const priceTrendObservables = items.map(item =>
          this.itemService.getPriceTrend(item.itemName, 1, timeSpan)
            .pipe(map(priceTrend => ({itemName: item.itemName, priceTrend: priceTrend})))
        );
        forkJoin(priceTrendObservables).subscribe(priceTrends => {
          priceTrends.sort((a, b) => {
            if (a.priceTrend != undefined && b.priceTrend != undefined) {
              const trend1 = a.priceTrend.percentagePriceChange;
              const trend2 = b.priceTrend.percentagePriceChange;
              if (trend2 > trend1) return 1;
              if (trend2 < trend1) return -1;
              return 0;
            } else {
              return 0;
            }
          });
          const sortedTrends: PriceTrendItem[] = priceTrends;
          this.priceTrendItems = sortedTrends.slice(0, 5);
          this.log(this.priceTrendItems.toString());

          sortedTrends.sort((a, b) => {
            if (a.priceTrend != undefined && b.priceTrend != undefined) {
              const trend1 = a.priceTrend.absolutePriceChange;
              const trend2 = b.priceTrend.absolutePriceChange;
              if (trend2 > trend1) return 1;
              if (trend2 < trend1) return -1;
              return 0;
            } else {
              return 0;
            }
          });
          this.priceTrendItemsAbsolute = sortedTrends.slice(0, 5);
          this.log(this.priceTrendItemsAbsolute.toString());
        })
      });
  }

  private log(message: string) {
    this.messageService.add(`Dashboard: ${message}`);
  }

  protected readonly ChronoUnits = ChronoUnits;
}
