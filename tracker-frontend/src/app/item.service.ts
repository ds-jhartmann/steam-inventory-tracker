import {Injectable} from '@angular/core';
import {Item} from "./items/item";
import {Observable, of} from "rxjs";
import {MessageService} from "./message.service";
import {HttpClient} from "@angular/common/http";
import {catchError, tap} from 'rxjs/operators';
import {PriceHistory} from "./items/priceHistory";
import {PriceTrend} from "./items/priceTrend";
import {environment} from "../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ItemService {
  private baseUrl = `${environment.apiUrl}/api/v1/items`;
  private getItemPath = '/getItem?name='
  private getPriceHistoryPath = '/priceHistory?name='
  private getAllItemsPath = '/all'
  private priceTrendPath = '/priceTrend'

  constructor(private messageService: MessageService, private http: HttpClient) {
  }

  getItems(): Observable<Item[]> {
    const url = this.baseUrl + this.getAllItemsPath;
    this.log("Requesting Items: " + url)
    return this.http.get<Item[]>(url).pipe(tap(_ => this.log("Fetched Items")),
      catchError(this.handleError<Item[]>('getItems', []))
    );
  }

  getItem(name: string): Observable<Item> {
    const url = this.baseUrl + this.getItemPath + encodeURIComponent(name);
    this.log("Requesting Item: " + url)
    return this.http.get<Item>(url).pipe(
      tap(_ => this.log("Fetched Item: " + name)),
      catchError(this.handleError<Item>('getItems',))
    );
  }

  getPriceHistory(name: string): Observable<PriceHistory[]> {
    const url = this.baseUrl + this.getPriceHistoryPath + encodeURIComponent(name);
    this.log("Requesting Price History: " + url)
    return this.http.get<PriceHistory[]>(url).pipe(
      tap(_ => this.log("Fetched Price History for Item: " + name)),
      catchError(this.handleError<PriceHistory[]>('getItems',))
    );
  }

  getPriceTrend(name: string, timespan: number, chronoUnit: string): Observable<PriceTrend> {
    const url = this.baseUrl + this.priceTrendPath + "?name=" + encodeURIComponent(name) + "&timespan=" + timespan + "&chronoUnit=" + chronoUnit;
    this.log("Requesting Price Trend: " + url)
    return this.http.get<PriceTrend>(url).pipe(
      tap(_ => this.log("Fetched Price Trend for Item: " + name)),
      catchError(this.handleError<PriceTrend>('getItems',))
    );
  }

  private log(message: string) {
    this.messageService.add(`ItemService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error);
      this.log(`${operation} failed: ${error.message}`);
      return of(result as T);
    };
  }
}
