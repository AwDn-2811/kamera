const express = require('express');
const cors = require('cors');
const midtransClient = require('midtrans-client');

const app = express();
app.use(cors());
app.use(express.json());

// 🔥 GANTI KE SERVER KEY & CLIENT KEY DEV PUNYA MU
let snap = new midtransClient.Snap({
    isProduction: false,
    serverKey: "Mid-server-_fDzTVNeM1s-tHEznTNu-UgN",
    clientKey: "Mid-client-_T33AptJiixrDdij"
});

app.get("/", (req, res) => {
    res.send("Midtrans backend aktif!");
});

app.post("/create-transaction", async (req, res) => {
    try {
        let parameter = {
            transaction_details: {
                order_id: req.body.order_id,
                gross_amount: req.body.gross_amount
            }
        };

        let transaction = await snap.createTransaction(parameter);

        res.json({
            redirect_url: transaction.redirect_url
        });

    } catch (err) {
        console.error("Midtrans Error:", err);
        res.status(500).json({ error: "Midtrans failed" });
    }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log("Server running on port " + port));
