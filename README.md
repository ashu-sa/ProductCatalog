# Product Catalog & Offline Cart

An Android app built for the Spire Lab Android Developer assessment. It fetches products from the
[DummyJSON Products API](https://dummyjson.com/docs/products), supports browsing, searching and
product details, and provides a locally persisted shopping cart that works fully offline.

## Features

- **Product listing** — 2-column grid with image, name, price and rating; loading, empty, error and
  retry states; offline banner; optional category filter chips.
- **Product search** — debounced (500 ms) search via `GET /products/search?q=...`; empty-result and
  error states.
- **Product details** — image, name, description, price, rating, category, brand and stock, fetched
  via `GET /products/{id}`; Add to Cart with Snackbar confirmation.
- **Shopping cart** — add, increase/decrease quantity, remove, clear; totals (item count + price);
  cart badge on the listing screen.
- **Offline support** — cart is stored in Room and works with no connectivity (view, edit
  quantities, remove, totals). Connectivity status is observed via `ConnectivityManager` and shown
  as a banner. The app + cart state survive process death / reopen.

## Setup / Build Instructions

Prerequisites: JDK 17+, Android SDK with Platform 35 + Build-Tools, internet for first Gradle sync.

```bash
# 1. Clone
git clone <your-repo-url>
cd ProductCatalog

# 2. Point Gradle at your SDK (or set ANDROID_HOME / ANDROID_SDK_ROOT)
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

# 3. Build debug APK
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk

# 4. Install on a connected device / emulator
./gradlew installDebug
# or: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open the project in Android Studio (Ladybug or newer): **File → Open → ProductCatalog**,
let Gradle sync, then Run.

## Architecture

**MVVM + Repository**, single-activity Compose app:

```
MainActivity (Compose entry)
└── CatalogNavHost (Navigation-Compose: list → detail/{id} → cart)
    ├── ProductListScreen  ← ProductListViewModel
    ├── ProductDetailScreen ← ProductDetailViewModel
    └── CartScreen          ← CartViewModel (shared, also feeds cart badge)

ProductRepository ── Retrofit ProductApi ── https://dummyjson.com/
CartRepository    ── Room CartDao ── catalog_cart.db (cart_items)
DI: AppContainer (manual, created in CatalogApp Application class)
Async: Kotlin Coroutines + StateFlow; search debounced with delay(500ms)
```

- `util/AppResult.kt` — `Success` / `Error(message, isNetwork)` wrapper; repository maps
  `UnknownHostException` → "No internet…", `SocketTimeoutException` → timeout message,
  `HttpException` → server error, etc.
- `util/Connectivity.kt` — `observeConnectivity()` Flow via `registerDefaultNetworkCallback`.
- Cart writes go through Room (`Flow<List<CartItemEntity>>`), so every screen reacts to changes
  and data survives app restarts.

## Libraries Used

| Area | Library | Version |
|---|---|---|
| UI | Jetpack Compose (BOM) + Material3 | 2024.10.00 |
| Navigation | navigation-compose | 2.7.3 |
| Lifecycle | lifecycle-runtime-ktx, lifecycle-viewmodel-compose | 2.8.6 |
| Networking | Retrofit + Gson converter, OkHttp + logging-interceptor | 2.11.0 / 4.12.0 |
| Persistence | Room (runtime + ktx, KSP compiler) | 2.6.1 |
| Images | Coil Compose | 2.6.0 |
| Async | kotlinx-coroutines-android | 1.9.0 |
| Build | AGP 8.7.3, Kotlin 2.0.21, KSP 2.0.21-1.0.25, Gradle 8.9, compileSdk/targetSdk 35, minSdk 24 | — |

## Local Storage Approach

- **Room database** `catalog_cart.db`, single table `cart_items` (`CartItemEntity` keyed by
  `productId`: title, price snapshot, thumbnail, stock, quantity, addedAt).
- `CartDao.observeItems(): Flow<...>` drives the cart UI and totals reactively.
- Quantity 0 = delete; price snapshot stored at add-time so totals stay correct offline even if the
  API is unreachable. No product-list caching (out of scope) — only the cart is persisted, per spec.

## Important Design Decisions

1. **No Hilt/Dagger** — manual `AppContainer` keeps the submission small, deterministic and easy to
   review; still cleanly separated (data / repository / ViewModel / UI).
2. **Price snapshot in cart** — guarantees offline totals without network; trade-off is staleness if
   server prices change.
3. **Categories handled leniently** — DummyJSON returns `[{slug,name,url}]`; a custom Gson
   deserializer also accepts plain strings, and category filtering is client-side so it never
   breaks the list if the endpoint changes.
4. **Debounced search (500 ms)** instead of per-keystroke requests; clearing the query restores the
   full list without a network call.
5. **Timeouts (15 s)** on OkHttp + mapped error messages + Retry buttons on every remote screen.
6. **minSdk 24** for broad coverage; Compose + Material3 throughout (no XML) for concise UI code.

## Known Limitations

- Product list itself is not cached — going offline on the listing screen shows the error/offline
  state with Retry (cart remains fully usable, per requirements).
- Pagination is fixed at `limit=60` for the listing; no infinite scroll.
- Category filter applies client-side to loaded products rather than `GET /products/category/{slug}`.
- No login/checkout flow — cart totals only, as specified.
- Images require network (Coil memory/disk cache only); offline placeholders show the last cached
  image when available.

## Screen Recording Checklist (for the submission form)

Record showing: listing (loading → content, plus error/retry by toggling airplane mode),
search with results + empty query, details screen, add-to-cart, quantity +/- , remove, totals,
airplane-mode cart edits, then kill + reopen app to prove persistence.
