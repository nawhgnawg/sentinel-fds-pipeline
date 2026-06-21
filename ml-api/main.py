from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

# 스프링 부트에서 넘어올 데이터의 형태(구조) 정의
class Transaction(BaseModel):
    accountNumber: str

    amount: int
    clientIp: str
    patternDescription: str
    type: str

@app.post("/predict")
def predict_fraud(transaction: Transaction):
    """
    실무에서는 여기에 Scikit-learn이나 TensorFlow 등 학습된 ML 모델이 들어갑니다.
    지금은 실증을 위한 모의(Mock) AI 확률 계산 로직을 작성합니다.
    """
    probability = 0.01  # 기본 사기 확률 1%

    # AI의 판단 로직 (패턴과 금액을 분석)
    if transaction.patternDescription != "NORMAL_TRANSACTION":
        probability += 0.70  # 이상 패턴이면 70% 증가

    if transaction.amount > 1000000:
        probability += 0.20  # 100만 원 이상 고액이면 20% 증가

    probability = min(probability, 0.99) # 최대 99%

    return {
        "fraud_probability": round(probability, 2),
        "is_fraud": probability >= 0.85  # AI가 85% 이상 확신할 때만 '사기(True)'로 판정
    }