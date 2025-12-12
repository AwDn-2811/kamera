{
  "bookings": {
    "bookingId": {
      "cameraId": "",
      "created_at": 0,
      "end_date": 0,
      "renterId": "",
      "start_date": 0,
      "status": "",
      "total_price": 0
    }
  },
  "cameras": {
    "cameraId": {
      "address": "",
      "brand": "",
      "createdAt": 0,
      "imageUrl": "",
      "isAvailable": true,
      "location": "",
      "model": "",
      "ownerId": "",
      "price": "",
      "pricePerDay": 0,
      "ratingAverage": 0,
      "resolution": "",
      "reviewCount": 0,
      "reviews": {
        "reviewId": {
          "comment": "",
          "rating": 0,
          "timestamp": 0,
          "userID": "",
          "userName": "",
          "userPhoto": ""
        }
      },
      "type": ""
    }
  },
  "feedbacks": {
    "feedbackId": {
      "message": "",
      "timestamp": 0,
      "userID": ""
    }
  },
  "reviews": {
    "cameraId": {
      "reviewId": {
        "comment": "",
        "rating": 0,
        "timestamp": 0,
        "userAvatarUrl": "",
        "userID": "",
        "userName": ""
      }
    }
  },
  "users": {
    "62b55cba-8935-40fa-995a-532b0377c7d3": {
      "dob": "28/11/2004",
      "email": "w@gmail.com",
      "firstName": "arya",
      "gender": "Male",
      "lastName": "wisnu",
      "password": "awdn2811",
      "phoneNumber": "9123823712",
      "userID": "62b55cba-8935-40fa-995a-532b0377c7d3"
    },
    "userID": {
      "address": "",
      "created_at": 0,
      "email": "",
      "phoneNumber": "",
      "profile_image_url": "",
      "username": ""
    }
  }
}

struktur database di atas dipakai untuk menyimpan data dari aplikasi dan URL gambar di cloudinary & Firebase

**BackEnd Code Midtrans:** 

const express = require("express");
const cors = require("cors");
const midtransClient = require("midtrans-client");

const app = express();
app.use(cors());
app.use(express.json());

// MIDTRANS CONFIG
let snap = new midtransClient.Snap({
isProduction: false,
serverKey: "GANTI DENGAN SERVER KEY MIDTRANS(SandBox)ANDA",
clientKey: "GANTI DENGAN CLIENT KEY MIDTRANS(SandBox)ANDA"
});

// CREATE TRANSACTION
app.post("/create-transaction", async (req, res) => {

    try {
        const { order_id, gross_amount } = req.body;

        if (!order_id || !gross_amount) {
            return res.status(400).json({
                error: "order_id atau gross_amount tidak valid"
            });
        }

        const parameter = {
            transaction_details: {
                order_id: order_id,
                gross_amount: parseInt(gross_amount)
            },
            callbacks: {
        finish: "Rental://payment-finished"
             }
        };

        const transaction = await snap.createTransaction(parameter);

        return res.json({
            redirect_url: transaction.redirect_url
        });

    } catch (err) {
        console.error("MIDTRANS ERROR:", err);
        return res.status(500).json({
            error: "Midtrans gagal membuat transaksi",
            detail: err.message
        });
    }
});

// RUN SERVER
app.listen(3000, "0.0.0.0", () => console.log("Server jalan"));

**Catatan:** 
1. Kode BackEnd di atas memerlukan instalasi **Node JS** agar bisa menggunakan pembayaran melalui **MidTrans,**
2. Kemudian file **.js** dengan nama yang anda inginkan contoh: **index.js**, Kemudian copy paste kode BackEnd di atas ke dalam file tersebut.
3. Setelah itu masuk ke Command Prompt (CMD) lalu masuk ke direktori folder tempat anda menyimpan file **index.js** kemudian ketik **node index.js** untuk menjalankan BackEnd nya.