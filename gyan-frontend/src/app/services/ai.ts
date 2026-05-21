import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AIService {

  private baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  askQuestion(question: string) {
    return this.http.post(`${this.baseUrl}/ai/ask`, {
      question: question
    });
  }
}