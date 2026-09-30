import os

import requests
from dash import Dash, Input, Output, dcc, html


API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/v1")
API_KEY = os.getenv(
    "API_KEY",
    "540ba996-2ae9-4f72-8b4f-014b463bc4d7",
)

app = Dash(__name__, suppress_callback_exceptions=True)
server = app.server

PAGE_STYLE = {
    "fontFamily": "Arial, sans-serif",
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
                },
            ),
        ],
        style={**PAGE_STYLE, "textAlign": "center", "paddingTop": "160px"},
    )


def item_card(item):
    price = item.get("basePrice", 0)
    return html.Article(
        [
            html.H2(item.get("name", "Unnamed item"), style={"marginTop": 0}),
            html.P(item.get("category", "Uncategorized").title()),
            html.Strong(f"${price:,.2f}"),
            html.P(
                f"Item ID: {item.get('id', 'N/A')}",
                style={"fontSize": "12px", "color": "#666"},
            ),
        ],
        style={
            "border": "1px solid #ddd",
            "borderRadius": "6px",
            "padding": "20px",
            "backgroundColor": "white",
        },
    )


def store_page():
    try:
        response = requests.get(
            f"{API_BASE_URL.rstrip('/')}/items",
            headers={"X-API-Key": API_KEY},
            timeout=5,
        )
        response.raise_for_status()
        items = response.json()
        content = (
            html.Div(
                [item_card(item) for item in items],
                style={
                    "display": "grid",
                    "gridTemplateColumns": "repeat(auto-fit, minmax(220px, 1fr))",
                    "gap": "16px",
                },
            )
            if items
            else html.P("There are no items in the store yet.")
        )
    except (requests.RequestException, ValueError) as error:
        content = html.P(
            f"Could not load items from the store API: {error}",
            style={"color": "#b00020"},
        )

    return html.Main(
        [
            dcc.Link("← Home", href="/"),
            html.H1("Store Items"),
            content,
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


if __name__ == "__main__":
    app.run(debug=True)
