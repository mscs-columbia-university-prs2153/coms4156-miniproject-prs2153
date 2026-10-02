import os

import requests
from dash import ALL, Dash, Input, Output, State, ctx, dcc, html, no_update


API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/v1")
API_KEY = os.getenv(
    "API_KEY",
    "540ba996-2ae9-4f72-8b4f-014b463bc4d7", # definitely don't do this in prod
)

app = Dash(__name__, suppress_callback_exceptions=True)
server = app.server

PAGE_STYLE = {
    "fontFamily": "Arial, sans-serif",
    "fontSize": "18px",
    "maxWidth": "1000px",
    "margin": "0 auto",
    "padding": "48px 24px",
    "color": "#222",
}


def home_page():
    return html.Main(
        [
            html.H1("Welcome to Simple Shop"),
            html.P("Browse our small collection of everyday items."),
            dcc.Link(
                "Enter Store",
                href="/store",
                style={
                    "display": "inline-block",
                    "marginTop": "16px",
                    "padding": "12px 20px",
                    "backgroundColor": "#222",
                    "color": "white",
                    "textDecoration": "none",
                    "borderRadius": "4px",
                    "fontSize": "17px",
                },
            ),
        ],
        style={**PAGE_STYLE, "textAlign": "center", "paddingTop": "160px"},
    )


def item_card(item, calculated_tax="N/A"):
    price = item.get("basePrice", 0)

    def detail_row(label, value, value_style=None):
        return html.Div(
            [
                html.Span(label, style={"fontWeight": "bold"}),
                html.Span(value, style=value_style or {}),
            ],
            style={
                "display": "flex",
                "justifyContent": "space-between",
                "gap": "16px",
                "padding": "8px 0",
                "borderBottom": "1px solid #eee",
            },
        )

    return html.Article(
        [
            html.H2(
                item.get("name", "Unnamed item"),
                style={"marginTop": 0, "marginBottom": "4px"},
            ),
            html.P(
                item.get("id", "N/A"),
                style={
                    "marginTop": 0,
                    "fontSize": "13px",
                    "color": "#999",
                    "overflowWrap": "anywhere",
                },
            ),
            html.Div(
                [
                    detail_row(
                        "Category",
                        item.get("category", "Uncategorized").title(),
                    ),
                    detail_row("Price", f"${price:,.2f}"),
                    detail_row(
                        "Calculated tax",
                        calculated_tax,
                        {"color": "#1261a0", "fontWeight": "bold"},
                    ),
                ],
            ),
            html.Button(
                "Remove Item",
                id={"type": "remove-item", "index": item.get("id")},
                n_clicks=0,
                style={
                    "marginTop": "auto",
                    "padding": "8px 12px",
                    "backgroundColor": "#b00020",
                    "color": "white",
                    "border": "none",
                    "borderRadius": "4px",
                    "cursor": "pointer",
                    "fontSize": "16px",
                    "alignSelf": "flex-end",
                },
            ),
        ],
        style={
            "border": "1px solid #ddd",
            "borderRadius": "6px",
            "padding": "20px",
            "backgroundColor": "white",
            "display": "flex",
            "flexDirection": "column",
            "minHeight": "300px",
        },
    )


def calculate_item_tax(item_id, state):
    if not state:
        return "N/A"

    try:
        response = requests.post(
            f"{API_BASE_URL.rstrip('/')}/tax/quote",
            headers={"X-API-Key": API_KEY},
            json={"state": state, "itemId": item_id},
            timeout=5,
        )
        response.raise_for_status()
        tax_amount = response.json().get("taxAmount")
        return f"${float(tax_amount):,.2f}" if tax_amount is not None else "N/A"
    except (requests.RequestException, ValueError, TypeError):
        return "N/A"


def load_supported_states():
    try:
        response = requests.get(
            f"{API_BASE_URL.rstrip('/')}/supported",
            headers={"X-API-Key": API_KEY},
            timeout=5,
        )
        response.raise_for_status()
        return response.json().get("states", [])
    except (requests.RequestException, ValueError):
        return []


def load_item_grid(state):
    try:
        response = requests.get(
            f"{API_BASE_URL.rstrip('/')}/items",
            headers={"X-API-Key": API_KEY},
            timeout=5,
        )
        response.raise_for_status()
        items = response.json()
        return (
            html.Div(
                [
                    item_card(
                        item,
                        calculate_item_tax(item.get("id"), state),
                    )
                    for item in items
                ],
                style={
                    "display": "grid",
                    "gridTemplateColumns": "repeat(3, minmax(0, 1fr))",
                    "gap": "16px",
                },
            )
            if items
            else html.P("There are no items in the store yet.")
        )
    except (requests.RequestException, ValueError) as error:
        return html.P(
            f"Could not load items from the store API: {error}",
            style={"color": "#b00020"},
        )


def store_page():
    states = load_supported_states()
    default_state = states[0] if states else None

    return html.Main(
        [
            dcc.Link("← Home", href="/"),
            html.Div(
                [
                    html.H1("Store Items"),
                    html.Div(
                        [
                            html.Label("Location", htmlFor="location-dropdown"),
                            dcc.Dropdown(
                                id="location-dropdown",
                                options=[
                                    {"label": state, "value": state}
                                    for state in states
                                ],
                                value=default_state,
                                placeholder="Select a state",
                                clearable=False,
                                style={"width": "170px", "textAlign": "left"},
                            ),
                        ],
                    ),
                ],
                style={
                    "display": "flex",
                    "justifyContent": "space-between",
                    "alignItems": "center",
                    "gap": "24px",
                },
            ),
            html.Div(id="action-message", style={"margin": "16px 0"}),
            dcc.Store(id="items-refresh", data=0),
            html.Div(id="item-grid"),
            html.Div(
                [
                    html.Button(
                        "Add Item",
                        id="show-add-form",
                        n_clicks=0,
                        style={
                            "padding": "10px 18px",
                            "cursor": "pointer",
                            "fontSize": "16px",
                            "backgroundColor": "#16733a",
                            "color": "white",
                            "border": "none",
                            "borderRadius": "4px",
                        },
                    ),
                    html.Div(
                        [
                            html.H2("Add an Item"),
                            dcc.Input(
                                id="item-name",
                                placeholder="Name",
                                type="text",
                                style={"padding": "8px", "fontSize": "16px"},
                            ),
                            dcc.Input(
                                id="item-category",
                                placeholder="Category",
                                type="text",
                                style={"padding": "8px", "fontSize": "16px"},
                            ),
                            dcc.Input(
                                id="item-price",
                                placeholder="Base price",
                                type="number",
                                min=0.01,
                                step=0.01,
                                style={"padding": "8px", "fontSize": "16px"},
                            ),
                            html.Button(
                                "Create Item",
                                id="submit-item",
                                n_clicks=0,
                                style={
                                    "padding": "9px 14px",
                                    "cursor": "pointer",
                                    "fontSize": "16px",
                                },
                            ),
                        ],
                        id="add-item-form",
                        style={
                            "display": "none",
                            "gap": "10px",
                            "margin": "20px auto",
                            "padding": "16px",
                            "border": "1px solid #ddd",
                            "maxWidth": "420px",
                        },
                    ),
                ],
                style={"textAlign": "center", "marginTop": "28px"},
            ),
        ],
        style=PAGE_STYLE,
    )


app.layout = html.Div(
    [
        dcc.Location(id="url", refresh=False),
        html.Div(id="page-content"),
    ]
)


@app.callback(Output("page-content", "children"), Input("url", "pathname"))
def display_page(pathname):
    if pathname == "/store":
        return store_page()
    return home_page()


@app.callback(
    Output("add-item-form", "style"),
    Input("show-add-form", "n_clicks"),
    prevent_initial_call=True,
)
def toggle_add_form(n_clicks):
    return {
        "display": "grid" if n_clicks % 2 else "none",
        "gap": "10px",
        "margin": "20px auto",
        "padding": "16px",
        "border": "1px solid #ddd",
        "maxWidth": "420px",
    }


@app.callback(
    Output("item-grid", "children"),
    Input("items-refresh", "data"),
    Input("location-dropdown", "value"),
)
def refresh_items(_refresh_count, state):
    return load_item_grid(state)


@app.callback(
    Output("action-message", "children"),
    Output("items-refresh", "data"),
    Output("show-add-form", "n_clicks"),
    Input("submit-item", "n_clicks"),
    Input({"type": "remove-item", "index": ALL}, "n_clicks"),
    State("item-name", "value"),
    State("item-category", "value"),
    State("item-price", "value"),
    State("items-refresh", "data"),
    prevent_initial_call=True,
)
def update_items(_submit_clicks, _remove_clicks, name, category, price, refresh_count):
    triggered_id = ctx.triggered_id
    headers = {"X-API-Key": API_KEY}

    try:
        if triggered_id == "submit-item":
            if not _submit_clicks:
                return "", refresh_count, no_update
            if not name or not category or price is None or price <= 0:
                return html.P(
                    "Enter a name, category, and positive base price.",
                    style={"color": "#b00020"},
                ), refresh_count, no_update

            response = requests.post(
                f"{API_BASE_URL.rstrip('/')}/items",
                headers=headers,
                json={"name": name, "category": category, "basePrice": price},
                timeout=5,
            )
            response.raise_for_status()
            message = f"Added {name}."
            add_form_clicks = 0
        elif (
            isinstance(triggered_id, dict)
            and triggered_id.get("type") == "remove-item"
            and any(_remove_clicks or [])
        ):
            item_id = triggered_id["index"]
            response = requests.delete(
                f"{API_BASE_URL.rstrip('/')}/items/{item_id}",
                headers=headers,
                timeout=5,
            )
            response.raise_for_status()
            message = "Item removed."
            add_form_clicks = no_update
        else:
            return "", refresh_count, no_update

        return (
            html.P(message, style={"color": "#16733a"}),
            refresh_count + 1,
            add_form_clicks,
        )
    except requests.RequestException as error:
        return html.P(
            f"The item could not be updated: {error}",
            style={"color": "#b00020"},
        ), refresh_count, no_update


if __name__ == "__main__":
    app.run(debug=True)
