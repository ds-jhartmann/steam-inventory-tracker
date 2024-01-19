import {Injectable} from '@angular/core';
import {Item} from "./items/item";
import {Observable, of} from "rxjs";
import {MessageService} from "./message.service";
import {HttpClient} from "@angular/common/http";
import {catchError, tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ItemService {
  private baseUrl = 'http://localhost:4200/api/v1/items';
  private getItemPath = '/getItem?name='
  private getAllItemsPath = '/all'

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
    const url = this.baseUrl + this.getItemPath + encodeURI(name);
    this.log("Requesting Item: " + url)
    return this.http.get<Item>(url).pipe(
      tap(_ => this.log("Fetched Item: " + name)),
      catchError(this.handleError<Item>('getItems',))
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
