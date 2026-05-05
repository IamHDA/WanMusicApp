# Music Label Service

Service FastAPI chay an de frontend gui file nhac va nhan ket qua gan nhan (labels).

## 1. Muc tieu

- Frontend upload 1 file am thanh len service.
- Service tra ve:
  - `POST /predict`: list string labels theo thu tu top-k (vd: `["acoustic","indie","lofi"]`).
  - `POST /predict/batch`: JSON ket qua tung file (chi tiet score, metadata).

## 2. Chay service local

Chay tu thu muc goc project (`D:/code/music`):

```bash
pip install -r service/requirements.txt
uvicorn service.app.main:app --host 127.0.0.1 --port 8000
```

Kiem tra:

- Health: `http://127.0.0.1:8000/health`
- Swagger docs: `http://127.0.0.1:8000/docs`

## 3. Cau hinh moi truong

- `LOG_LEVEL` (mac dinh: `INFO`)
- `ALLOWED_ORIGINS` (comma-separated, mac dinh: `*`)
- `REQUIRE_API_KEY` (`true/false`, mac dinh: `false`)
- `API_KEYS` (comma-separated key list, dung khi `REQUIRE_API_KEY=true`)
- `MAX_UPLOAD_MB` (mac dinh: `50`)
- `DEFAULT_TOP_K` (mac dinh: `7`)
- `MAX_TOP_K` (mac dinh: `10`)
- `MAX_SEGMENTS` (mac dinh: `5`)
- `MAX_CONCURRENT_PREDICT` (mac dinh: `2`)
- `RATE_LIMIT_PER_MINUTE` (mac dinh: `120`, dat `0` de tat)
- `UNKNOWN_LABEL` (mac dinh: `unknown`)
- `MIN_CONFIDENCE_DEFAULT` (mac dinh: `0.0`)
- `ENABLE_CUDA_AUTOCAST` (`true/false`, mac dinh: `false`) bat FP16 infer tren CUDA de giam latency
- `ENABLE_TORCH_COMPILE` (`true/false`, mac dinh: `false`) bat `torch.compile` cho model (nen benchmark truoc khi bat production)

Vi du PowerShell truoc khi chay:

```powershell
$env:REQUIRE_API_KEY="true"
$env:API_KEYS="frontend-secret-1"
$env:ALLOWED_ORIGINS="http://localhost:3000,https://your-frontend-domain.com"
$env:MAX_UPLOAD_MB="30"
$env:MAX_CONCURRENT_PREDICT="2"
$env:RATE_LIMIT_PER_MINUTE="120"
$env:MIN_CONFIDENCE_DEFAULT="0.45"
```

## 4. API contract cho frontend

### `GET /health`

Tra trang thai san sang va thong tin model.

### `GET /labels`

Tra danh sach nhan ma model ho tro.

### `GET /metrics`

Tra metrics runtime (`predict_requests`, `predict_errors`, `avg_latency_ms`, ...).

### `POST /predict`

- Content-Type: `multipart/form-data`
- Field bat buoc: `file`
- Query optional:
  - `top_k` (1..`MAX_TOP_K`)
  - `min_confidence` (0..1)

Response mau (JSON list string):

```json
["acoustic", "indie", "lofi", "vpop", "trap"]
```

### `POST /predict/batch`

- Content-Type: `multipart/form-data`
- Field bat buoc: `files` (nhieu file)
- Query tuong tu `/predict`

Tra ve ket qua tung file (`ok/result/error`).

## 5. Vi du goi tu frontend (JavaScript/TypeScript)

```ts
async function predictMusic(file: File) {
  const form = new FormData();
  form.append("file", file);

  const res = await fetch(
    "http://127.0.0.1:8000/predict?top_k=7&min_confidence=0.45",
    {
      method: "POST",
      headers: {
        "x-api-key": "frontend-secret-1"
      },
      body: form
    }
  );

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Predict failed: ${res.status} ${text}`);
  }

  return res.json();
}
```

Neu `REQUIRE_API_KEY=false` thi bo header `x-api-key`.

## 6. Ma loi thuong gap

- `400`: file rong.
- `413`: file qua lon hoac batch qua nhieu file.
- `415`: sai content-type/extension.
- `422`: khong decode duoc audio hoac khong trich xuat duoc segment hop le.
- `429`: vuot rate limit.
- `503`: model chua san sang.

## 7. Luu y van hanh service chay an

- Nen bind `127.0.0.1` hoac private network, khong expose public truc tiep.
- Neu frontend goi truc tiep thi bat API key va gioi han origin.
- Theo doi `/metrics` de tinh chinh throughput.
- Neu response la `unknown`, frontend nen cho phep user/chuyen vien chon nhan lai.
