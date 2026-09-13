# BomDemais — Design Specification

Master reference for all visual and structural design decisions. The project conforms to this document — not the other way around. Update here first, then update the code.

---

## 1. Brand Colors (Anchors)

Three source colors that feed the MD3 tonal system, plus one fixed semantic highlight.

| Role | Hex | Character |
|---|---|---|
| Primary | `#6B3A80` | Deep brand purple |
| Secondary | `#854B70` | Plum rose |
| Tertiary | `#A63A32` | Bold brick red |
| Highlight | `#FFBD5F` | Golden amber — fixed semantic, outside tonal system |

**Notes:**
- Palette is brand-derived, cross-referenced with the actual Bom Demais logo (vivid purple background, warm orange/yellow circle, bright yellow text)
- Direction is softer and more app-appropriate than the logo's high saturation, while preserving the warm purple identity
- Highlight (`#FFBD5F`) is used for badges, labels, and financial positives — never as a tonal role

---

## 2. MD3 Color Token Table (Light Theme Only)

Approximate values from the MD3 tonal system. **Verify with [Material Theme Builder](https://m3.material.io/theme-builder) before writing to code.**

### Primary — from `#6B3A80`

| Token | Hex |
|---|---|
| `colorPrimary` | `#7B3D9C` |
| `colorOnPrimary` | `#FFFFFF` |
| `colorPrimaryContainer` | `#F3D9FF` |
| `colorOnPrimaryContainer` | `#28003F` |

### Secondary — from `#854B70`

| Token | Hex |
|---|---|
| `colorSecondary` | `#864D72` |
| `colorOnSecondary` | `#FFFFFF` |
| `colorSecondaryContainer` | `#FFD8EF` |
| `colorOnSecondaryContainer` | `#36002B` |

### Tertiary — from `#A63A32`

| Token | Hex |
|---|---|
| `colorTertiary` | `#A63A32` |
| `colorOnTertiary` | `#FFFFFF` |
| `colorTertiaryContainer` | `#FFDAD7` |
| `colorOnTertiaryContainer` | `#3C0400` |

### Error — MD3 standard

| Token | Hex |
|---|---|
| `colorError` | `#B3261E` |
| `colorOnError` | `#FFFFFF` |
| `colorErrorContainer` | `#F9DEDC` |
| `colorOnErrorContainer` | `#410E0B` |

### Surface & Neutral — derived from primary hue, desaturated

| Token | Hex |
|---|---|
| `android:colorBackground` | `#FFF7FF` |
| `colorOnBackground` | `#1D1B1E` |
| `colorSurface` | `#FFF7FF` |
| `colorOnSurface` | `#1D1B1E` |
| `colorSurfaceVariant` | `#EDE0EF` |
| `colorOnSurfaceVariant` | `#4A454E` |
| `colorOutline` | `#7C7481` |
| `colorOutlineVariant` | `#CDC4D0` |
| `colorInverseSurface` | `#322F35` |
| `colorInverseOnSurface` | `#F5EFF7` |
| `colorInversePrimary` | `#E4ADFF` |
| `colorScrim` | `#000000` |

### Custom — fixed, outside tonal system

| Name | Hex | Usage |
|---|---|---|
| `colorHighlight` | `#FFBD5F` | Badges, financial positives, special labels |

---

## 3. Typography

**Font family:** Nunito (Google Fonts)

Chosen for its rounded terminals (matches brand warmth and logo personality), excellent readability at small sizes, and wide weight range (300–800).

### Type Scale

| Role | Size | Weight | Line Height | Letter Spacing | Primary Usage |
|---|---|---|---|---|---|
| `displayLarge` | 57sp | Regular 400 | 64sp | −0.25sp | — |
| `displayMedium` | 45sp | Regular 400 | 52sp | 0sp | Monthly profit hero number |
| `displaySmall` | 36sp | Regular 400 | 44sp | 0sp | Monthly totals |
| `headlineLarge` | 32sp | SemiBold 600 | 40sp | 0sp | — |
| `headlineMedium` | 28sp | SemiBold 600 | 36sp | 0sp | Screen section headers |
| `headlineSmall` | 24sp | SemiBold 600 | 32sp | 0sp | Category names |
| `titleLarge` | 22sp | Bold 700 | 28sp | 0sp | Toolbar screen title |
| `titleMedium` | 16sp | Bold 700 | 24sp | 0.15sp | Card titles, product names |
| `titleSmall` | 14sp | Bold 700 | 20sp | 0.1sp | Secondary card info |
| `bodyLarge` | 16sp | Regular 400 | 24sp | 0.5sp | List item text, note body |
| `bodyMedium` | 14sp | Regular 400 | 20sp | 0.25sp | Descriptions, timestamps |
| `bodySmall` | 12sp | Regular 400 | 16sp | 0.4sp | Metadata, fine print |
| `labelLarge` | 14sp | SemiBold 600 | 20sp | 0.1sp | Button text, chips |
| `labelMedium` | 12sp | SemiBold 600 | 16sp | 0.5sp | Tab labels, badges |
| `labelSmall` | 11sp | SemiBold 600 | 16sp | 0.5sp | Tiny status indicators |

---

## 4. Shape System

**Direction:** Soft and approachable — rounded end of the scale throughout. Sharp corners only where forced by component convention.

### Token Scale

| Token | dp | Departure from MD3 default |
|---|---|---|
| `ShapeExtraSmall` | 4dp | Standard |
| `ShapeSmall` | 8dp | Standard |
| `ShapeMedium` | 16dp | +4dp (default is 12dp) |
| `ShapeLarge` | 20dp | +4dp (default is 16dp) |
| `ShapeExtraLarge` | 28dp | Standard |
| `ShapeFull` | pill | Standard |

### Component Assignments

| Component | Token | dp | Notes |
|---|---|---|---|
| Button (all styles) | Full | pill | |
| FAB | Large | 20dp | |
| Extended FAB | Large | 20dp | |
| Card | Medium | 16dp | Primary surface — drives overall softness |
| Chip | Small | 8dp | |
| Dialog | ExtraLarge | 28dp | |
| Bottom sheet | Large | 20dp | Top corners only |
| Navigation drawer | Large | 20dp | Right corners only |
| Snackbar | ExtraSmall | 4dp | |
| Text field (outlined) | ExtraSmall | 4dp | |
| Text field (filled) | ExtraSmall top | 4dp | Flat bottom edge |
| Switch | Full | pill | |
| Checkbox | ExtraSmall | 4dp | |
| Badge | Full | pill | |
| Bottom nav indicator | Full | pill | |
| List items | None | 0dp | No shape on flat list rows |

---

## 5. Navigation Architecture

### Auth Flow

```
App Launch
└── FirebaseAuth check
      ├── Not authenticated → LoginActivity
      │     ├── Email + password → MainActivity
      │     ├── "Esqueci minha senha" → ForgotPasswordActivity
      │     │     └── Enter email → Firebase reset email → back to Login
      │     └── [Google Sign-In — designed in, activated in a later phase]
      └── Authenticated → MainActivity (Estoque tab)

Logout
└── Toolbar avatar icon (all MainActivity screens)
      └── AccountBottomSheet
            ├── User email (non-interactive)
            └── "Sair" → FirebaseAuth.signOut() → LoginActivity (clear back stack)
```

### Top-Level Structure

Single `MainActivity`, four tabs via `BottomNavigationView`. Default tab on launch: **Estoque**.

```
MainActivity
└── BottomNavigationView (4 tabs)
    ├── [1] Estoque      icon: ic_inventory
    ├── [2] Lista        icon: ic_checklist
    ├── [3] Notas        icon: ic_edit_note
    └── [4] Financeiro   icon: ic_account_balance_wallet
```

### Full Screen Map

```
Estoque
└── CategoriasFragment (root)
      ├── FAB → AddEditCategoryBottomSheet (create)
      ├── Tap category → ProdutosFragment
      │     ├── FAB → AddEditProductBottomSheet (create)
      │     ├── Tap product → AddEditProductBottomSheet (edit)
      │     └── Swipe product → delete + undo Snackbar
      └── Long press category → delete + undo Snackbar

Lista de Compras
└── ListaDeComprasFragment (root, self-contained)
      ├── FAB → inline add field at bottom of list
      ├── Tap checkbox → checked/strikethrough state
      ├── Swipe item → delete + undo Snackbar
      └── Toolbar action → clear all checked items

Notas
└── NotasFragment (root)
      ├── FAB → NoteEditorFragment (new note)
      ├── Tap note → NoteEditorFragment (edit note)
      └── NoteEditorFragment
            ├── Title field + body field
            ├── Toolbar back → "Discard changes?" dialog if unsaved
            └── Toolbar action → delete note (with confirmation)

Financeiro
└── FinanceiroFragment (root)
      ├── Month selector (← previous / next →)
      ├── Summary card (Receita / Despesas / Lucro)
      ├── Entry list for selected month
      ├── FAB → AddTransactionBottomSheet
      │     ├── Type toggle: Receita | Despesa
      │     ├── Amount field
      │     ├── Description field
      │     ├── Date picker
      │     └── Save button
      ├── Tap entry → EditTransactionBottomSheet (same form, pre-filled)
      └── Swipe entry → delete + undo Snackbar
```

### Transitions

| Navigation event | Transition |
|---|---|
| Bottom nav tab switch | Cross-fade, 250ms |
| Drill-down (categories → products) | Slide in from right |
| Drill-down (notes list → editor) | Slide in from right |
| Back from drill-down | Slide out to right |
| Bottom sheet open | Slide up from bottom (standard sheet) |
| Bottom sheet dismiss | Slide down |
| Back on root tab (not Estoque) | Switch to Estoque tab |
| Back on Estoque tab | Exit app |

### Back Stack Rules

- Bottom nav tabs use **show/hide** (no back stack between tabs)
- Drill-down screens (ProdutosFragment, NoteEditorFragment) are added to the back stack
- NoteEditorFragment with unsaved changes: back triggers a "Discard changes?" confirmation dialog
- Logout clears the entire back stack before starting `LoginActivity`

---

## 6. Screen Wireframes

### Screen 0a — LoginActivity

```
┌──────────────────────────────────────┐
│                                      │
│         [Bom Demais logo]            │ ← brand logo, centered
│                                      │
│  ┌────────────────────────────────┐  │
│  │  Email                         │  │ ← outlined text field
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Senha                    [👁] │  │ ← password + show/hide toggle
│  └────────────────────────────────┘  │
│                                      │
│             Esqueci minha senha      │ ← labelLarge, primary, right-aligned
│                                      │
│  ┌────────────────────────────────┐  │
│  │            Entrar              │  │ ← primary filled button (pill)
│  └────────────────────────────────┘  │
│                                      │
│  ──────────────  ou  ──────────────  │ ← divider (for future Google auth)
│                                      │
│  ┌────────────────────────────────┐  │
│  │  [G]  Entrar com Google        │  │ ← outlined button, disabled for now
│  └────────────────────────────────┘  │
│                                      │
└──────────────────────────────────────┘
↑ No Toolbar, no BottomNav
```

### Screen 0b — ForgotPasswordActivity

```
┌──────────────────────────────────────┐
│  ←  Recuperar senha                  │ ← Toolbar (back to Login)
├──────────────────────────────────────┤
│                                      │
│  Digite o email da sua conta.        │ ← bodyLarge
│  Enviaremos um link de recuperação.  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │  Email                         │  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │        Enviar link             │  │ ← primary filled button
│  └────────────────────────────────┘  │
│                                      │
│  SUCCESS: Snackbar "Email enviado.   │
│  Verifique sua caixa de entrada."    │
└──────────────────────────────────────┘
```

### Screen 0c — AccountBottomSheet (logout)

```
┌──────────────────────────────────────┐
│           (dimmed backdrop)          │
│  ┌────────────────────────────────┐  │
│  │              ━━━               │  │ ← drag handle
│  │  [avatar]                      │  │ ← circular, primary bg, initials
│  │  marcelo@bomdemais.com         │  │ ← bodyMedium
│  │  ──────────────────────────    │  │
│  │  Sair                          │  │ ← text button, error color
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
Avatar = uppercase first letter of email.
Future: Google profile photo when Sign-In is added.
Triggered by avatar icon in MainActivity toolbar (all tabs).
```

### Screen 1 — CategoriasFragment

```
┌──────────────────────────────────────┐
│  Estoque                          ⋮  │ ← Toolbar (primary bg, white text)
├──────────────────────────────────────┤
│  ┌─────────────────┐ ┌─────────────┐ │
│  │    [icon]       │ │   [icon]    │ │
│  │    Bebidas      │ │   Doces     │ │
│  │    12 itens     │ │   8 itens   │ │
│  └─────────────────┘ └─────────────┘ │
│  ┌─────────────────┐ ┌─────────────┐ │
│  │    [icon]       │ │   [icon]    │ │
│  │    Coberturas   │ │   Insumos   │ │
│  │    5 itens      │ │   20 itens  │ │
│  └─────────────────┘ └─────────────┘ │
│                            ┌──────┐  │
│                            │  +   │  │ ← FAB (primary)
│                            └──────┘  │
├──────────────────────────────────────┤
│  [■]Estoque [☑]Lista [✎]Notas [₢]Fin│ ← BottomNav
└──────────────────────────────────────┘

EMPTY STATE:
│              [icon]                  │
│         Nenhuma categoria            │
│     Adicione uma para começar        │
```

### Screen 2 — ProdutosFragment

```
┌──────────────────────────────────────┐
│  ←  Bebidas                       ⋮  │ ← Toolbar (back arrow, category name)
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │  Açaí 500ml          ●  4 un  │  │ ← ● = low-stock dot (tertiary color)
│  │  Polpa congelada               │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Suco de Morango        24 un  │  │
│  │  Caixa 1L                      │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Água Mineral           ●  2 un│  │
│  │  Garrafa 500ml                 │  │
│  └────────────────────────────────┘  │
│                            ┌──────┐  │
│                            │  +   │  │ ← FAB (primary)
│                            └──────┘  │
└──────────────────────────────────────┘
↑ No BottomNav (drill-down screen)
● = quantity at or below minimum threshold
```

### Screen 3 — ListaDeComprasFragment

```
┌──────────────────────────────────────┐
│  Lista de Compras             [✓↑]   │ ← [✓↑] = clear checked action
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │  ☑  Açaí polpa        2 kg    │  │ ← checked (strikethrough text)
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  ☐  Leite condensado   4 un   │  │ ← unchecked
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  ☐  Granola            1 kg   │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  + Adicionar item...           │  │ ← inline add field (FAB reveals)
│  └────────────────────────────────┘  │
│                            ┌──────┐  │
│                            │  +   │  │ ← FAB (primary)
│                            └──────┘  │
├──────────────────────────────────────┤
│  [■]Estoque [☑]Lista [✎]Notas [₢]Fin│
└──────────────────────────────────────┘

EMPTY STATE:
│              [icon]                  │
│         Lista vazia                  │
│   Adicione itens para sua compra     │
```

### Screen 4 — NotasFragment

```
┌──────────────────────────────────────┐
│  Notas                            ⋮  │ ← Toolbar
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │  Pedido fornecedor             │  │ ← titleMedium
│  │  Ligar segunda-feira para...   │  │ ← bodySmall preview (1 line)
│  │                     23/04/2026 │  │ ← timestamp right-aligned
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Receita açaí especial         │  │
│  │  500g polpa, 200ml leite...    │  │
│  │                     20/04/2026 │  │
│  └────────────────────────────────┘  │
│                            ┌──────┐  │
│                            │  +   │  │ ← FAB (primary)
│                            └──────┘  │
├──────────────────────────────────────┤
│  [■]Estoque [☑]Lista [✎]Notas [₢]Fin│
└──────────────────────────────────────┘

EMPTY STATE:
│              [icon]                  │
│         Nenhuma anotação             │
│    Toque em + para criar uma nota    │
```

### Screen 5 — NoteEditorFragment

```
┌──────────────────────────────────────┐
│  ←                            [🗑]   │ ← back + delete action in toolbar
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │  Título                        │  │ ← titleLarge field, no border
│  └────────────────────────────────┘  │
│  ──────────────────────────────────  │ ← subtle divider
│                                      │
│  Escreva sua anotação aqui...        │ ← bodyLarge field, fills height
│                                      │
│                                      │
│                                      │
│  23 de abril de 2026, 14:32          │ ← bodySmall timestamp, bottom
└──────────────────────────────────────┘
↑ No BottomNav, no FAB
Auto-saves on back if content non-empty.
Back with unsaved changes → "Descartar alterações?" dialog.
```

### Screen 6 — FinanceiroFragment

```
┌──────────────────────────────────────┐
│  Financeiro                       ⋮  │ ← Toolbar
├──────────────────────────────────────┤
│                                      │
│        ←   Abril 2026   →           │ ← month selector
│                                      │
│  ┌────────────────────────────────┐  │
│  │  Receita        R$ 4.820,00    │  │
│  │  ─────────────────────────     │  │
│  │  Despesas       R$ 2.310,00    │  │
│  │  ─────────────────────────     │  │
│  │  Lucro        ● R$ 2.510,00    │  │ ← ● = highlight color dot
│  └────────────────────────────────┘  │ ← Summary card (primaryContainer bg)
│                                      │
│  Lançamentos                         │ ← headlineSmall
│                                      │
│  ┌────────────────────────────────┐  │
│  │  ↑ Venda do dia      R$320,00  │  │ ← ↑ receita (highlight color)
│  │  28/04                         │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  ↓ Fornecedor açaí  R$450,00   │  │ ← ↓ despesa (tertiary/error tone)
│  │  27/04                         │  │
│  └────────────────────────────────┘  │
│                            ┌──────┐  │
│                            │  +   │  │ ← FAB (primary)
│                            └──────┘  │
├──────────────────────────────────────┤
│  [■]Estoque [☑]Lista [✎]Notas [₢]Fin│
└──────────────────────────────────────┘

EMPTY STATE (no entries for month):
│  Summary card shows R$0,00 for all   │
│              [icon]                  │
│     Nenhum lançamento ainda          │
│   Toque em + para registrar          │
```

### Screen 7 — AddTransactionBottomSheet

```
┌──────────────────────────────────────┐
│                                      │
│           (dimmed backdrop)          │
│                                      │
│  ┌────────────────────────────────┐  │
│  │              ━━━               │  │ ← drag handle
│  │    Novo Lançamento             │  │ ← titleLarge
│  │                                │  │
│  │  ┌───────────┐ ┌────────────┐  │  │
│  │  │  Receita  │ │  Despesa   │  │  │ ← segmented toggle
│  │  └───────────┘ └────────────┘  │  │
│  │                                │  │
│  │  ┌──────────────────────────┐  │  │
│  │  │  R$  Valor               │  │  │ ← amount field
│  │  └──────────────────────────┘  │  │
│  │  ┌──────────────────────────┐  │  │
│  │  │  Descrição               │  │  │ ← description field
│  │  └──────────────────────────┘  │  │
│  │  ┌──────────────────────────┐  │  │
│  │  │  📅  28/04/2026          │  │  │ ← date field (opens DatePicker)
│  │  └──────────────────────────┘  │  │
│  │  ┌──────────────────────────┐  │  │
│  │  │         Salvar           │  │  │ ← primary filled button (Full/pill)
│  │  └──────────────────────────┘  │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
EditTransactionBottomSheet = same layout,
pre-filled fields, button "Salvar alterações",
delete icon in sheet header.
```

---

## 7. Component & Layout Standards

### Cards

- **List item cards → Filled** (`surfaceVariant` bg, no elevation, no border)
- **Summary/hero card (Financeiro) → Elevated** (level 1, `primaryContainer` bg)

### List Spacing

- 8dp vertical gap between cards, no dividers
- Content padding: 16dp horizontal, 8dp vertical (top of list)
- Card internal padding: 16dp all sides

### FAB Style

- **Regular FAB** (icon only) across all screens
- Upgrade Financeiro FAB to Extended ("+ Lançamento") only if usability testing shows confusion

### Empty States

- **Large MD icon (64dp, primary @ 60% alpha) + headlineSmall heading + bodyMedium subtitle**
- No illustrations
- Per-screen copy defined in wireframes (Section 6)

### Loading States

- **CircularProgressIndicator** centered in content area, primary color
- No shimmer/skeleton screens

### Confirmation Dialogs

`MaterialAlertDialogBuilder` — two patterns:

**Destructive action** (e.g. delete note from editor):
```
  Excluir nota?
  Esta ação não pode ser desfeita.
  [Cancelar]   [Excluir ← error color]
```

**Discard changes** (back from NoteEditor with unsaved content):
```
  Descartar alterações?
  As alterações não serão salvas.
  [Cancelar]   [Descartar]
```

### Swipe-to-Delete

Enabled on all list screens. Immediate deletion + Snackbar undo (5s timeout).

| Screen | Swipe target | Undo |
|---|---|---|
| ProdutosFragment | Product row | Yes |
| ListaDeComprasFragment | Item row | Yes |
| NotasFragment | Note card | Yes |
| FinanceiroFragment | Transaction row | Yes |

- NoteEditorFragment delete (toolbar) → confirmation dialog
- Category delete → confirmation dialog (contains products)
- Swipe reveals: errorContainer bg + trash icon (right side)

### Error States

- **Transient errors**: Snackbar + "Tentar novamente" action
- **Full load failure**: empty state layout with error icon + different copy
- **Auth errors**: silent redirect to LoginActivity

### Financial Entry Colors

| Entry type | Color | Token |
|---|---|---|
| Receita | Golden amber | `colorHighlight` #FFBD5F |
| Despesa | Brick red | `colorTertiary` #A63A32 |
| Lucro positivo | Highlight dot | `colorHighlight` |
| Lucro negativo | Error tone | `colorError` |

### Spacing & Touch Targets

| Property | Value |
|---|---|
| Screen horizontal padding | 16dp |
| Card internal padding | 16dp |
| Gap between cards | 8dp |
| Minimum touch target | 48dp |
| FAB margin from edge | 16dp |
| FAB margin from bottom nav | 8dp |
| Bottom nav height | 80dp |

### Icon Style

Material Symbols — **Outlined** variant throughout. Filled variant only for active bottom nav tab indicator.

---

## Phase 2 — Architecture & Project Setup

### Principles

- Firebase Realtime Database (no migration to Firestore)
- MVVM + Repository + Kotlin Flow — no changes
- Manual fragment transactions — no Navigation Component
- No DI framework — repositories instantiated directly in ViewModels
- Repositories are the **only** classes that touch Firebase

---

### Data Models

**Category**
```kotlin
data class Category(
    val id: String = "",
    val name: String = ""
)
```

**Product**
```kotlin
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",   // category name, denormalized
    val quantity: Int = 0,
    val unit: String = "un",
    val minQuantity: Int = 1
)
```

**Note**
```kotlin
data class Note(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0
)
```

**ShoppingItem**
```kotlin
data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val unit: String = "un",
    val isChecked: Boolean = false
)
```

**Transaction**
```kotlin
data class Transaction(
    val id: String = "",
    val type: String = "RECEITA",   // "RECEITA" | "DESPESA"
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = 0,
    val monthKey: String = ""       // "YYYY-MM" e.g. "2026-04"
)
```

---

### Firebase Realtime Database Schema

```
root/
├── categorias/{id}/
│     id, name
│
├── produtos/{id}/
│     id, name, category (name, denormalized), quantity, unit, minQuantity
│
├── notas/{id}/
│     id, title, body, timestamp
│
├── lista_compras/{id}/
│     id, name, quantity, unit, isChecked
│
└── transacoes/{id}/
      id, type, amount, description, date, monthKey ("YYYY-MM")
```

**Migration:** Existing dev data is incompatible with the new schema and will be wiped.

---

### Data Layer Rules

- `monthKey` always formatted as `"YYYY-MM"` — generated at write time
- Low-stock state computed as `quantity <= minQuantity` in ViewModel — never stored in Firebase
- Category stored as name string in Product (denormalized) — no joins

---

### Structural Changes

| Change | From | To |
|---|---|---|
| ProdutosActivity | Separate Activity | ProdutosFragment (back stack) |
| NoteEditorFragment | DialogFragment | Full-screen Fragment (back stack) |
| Category type | `String` | `Category` data class |
| Note fields | `text: String` | `title: String` + `body: String` |
| ShoppingItem quantity | `quantityToBuy: Int` | `quantity: Int` |

---

### New Files

| File | Purpose |
|---|---|
| `data/Transaction.kt` | Transaction data model |
| `data/FinanceiroRepository.kt` | CRUD + monthly query for transactions |
| `financeiro/FinanceiroFragment.kt` | Financeiro tab screen |
| `financeiro/FinanceiroViewModel.kt` | State + actions |
| `financeiro/FinanceiroAdapter.kt` | Transaction list adapter |

### Modified Files

| File | Change |
|---|---|
| `data/Product.kt` | Add `unit`, `minQuantity` |
| `data/Note.kt` | `text` → `title` + `body` |
| `data/ShoppingItem.kt` | `quantityToBuy` → `quantity`, add `unit` |
| `data/CategoriasRepository.kt` | String → Category object |
| `data/NotasRepository.kt` | `text` → `title` + `body` |
| `data/ProdutosRepository.kt` | Add `unit`, `minQuantity` handling |
| `data/ShoppingRepository.kt` | Renamed field + `unit` |
| `categorias/*` | String → Category throughout |
| `notas/NoteEditorFragment.kt` | DialogFragment → Fragment |
| `produtos/ProdutosActivity.kt` | → ProdutosFragment |
| `MainActivity.kt` | 4th tab + ProdutosFragment/NoteEditorFragment back stack |
| `res/menu/bottom_nav_menu.xml` | Add Financeiro item |

### Dependencies to Add

- Nunito font via `res/font/` (Android Downloadable Fonts — no Gradle dep)
- No other new dependencies required
