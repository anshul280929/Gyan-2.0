from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI()

model = SentenceTransformer("all-MiniLM-L6-v2")

class EmbeddingRequest(BaseModel):
    text: str


@app.post("/embedding")
def generate_embedding(request: EmbeddingRequest):
    embedding = model.encode(request.text).tolist()
    return {"embedding": embedding}