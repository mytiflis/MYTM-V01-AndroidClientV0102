const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.PORT || 3303;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Request logging
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
    next();
});

// Load data files
const dataDir = path.join(__dirname, 'data');
const users = JSON.parse(fs.readFileSync(path.join(dataDir, 'users.json'), 'utf8'));
const jobs = JSON.parse(fs.readFileSync(path.join(dataDir, 'jobs.json'), 'utf8'));
const forms = JSON.parse(fs.readFileSync(path.join(dataDir, 'forms.json'), 'utf8'));

// In-memory storage for dynamic data
let locationRequests = [];
let heartbeats = [];
let pendingOps = [];
let notifications = [
    { id: 1, tip: 'INFO', baslik: 'Hoş geldiniz', mesaj: 'SGC Teknisyen mobil uygulamasına hoş geldiniz', t: new Date().toISOString() }
];

// Auth helper
function authenticateToken(req, res, next) {
    const token = req.headers['x-token'];
    if (!token) {
        return res.status(401).json({ error: 'Token gerekli' });
    }
    // In mock, accept any non-empty token
    req.teknisyenId = 1; // Mock technician ID
    next();
}

// ============================================================
// AUTH ENDPOINTS
// ============================================================

app.post('/api/auth/login', (req, res) => {
    const { username, password } = req.body;
    
    const user = users.find(u => u.username === username && u.password === password);
    if (!user) {
        return res.status(401).json({ error: 'Geçersiz kullanıcı adı veya şifre' });
    }

    const token = 'mock-token-' + uuidv4();
    
    res.json({
        token,
        user: {
            id: user.id,
            username: user.username,
            email: user.email,
            fullName: user.fullName,
            role: user.role
        },
        teknisyen: {
            id: user.teknisyenId,
            userId: user.id,
            firstName: user.firstName,
            lastName: user.lastName,
            phone: user.phone,
            isActive: true
        }
    });
});

app.post('/api/auth/logout', authenticateToken, (req, res) => {
    res.json({ success: true });
});

app.get('/api/auth/me', authenticateToken, (req, res) => {
    const user = users.find(u => u.teknisyenId === req.teknisyenId);
    if (!user) {
        return res.status(404).json({ error: 'Kullanıcı bulunamadı' });
    }
    res.json({
        user: {
            id: user.id,
            username: user.username,
            email: user.email,
            fullName: user.fullName,
            role: user.role
        },
        teknisyen: {
            id: user.teknisyenId,
            userId: user.id,
            firstName: user.firstName,
            lastName: user.lastName,
            phone: user.phone,
            isActive: true
        }
    });
});

// ============================================================
// HEARTBEAT
// ============================================================

app.post('/api/heartbeat', authenticateToken, (req, res) => {
    const heartbeat = {
        id: uuidv4(),
        teknisyenId: req.teknisyenId,
        ...req.body,
        receivedAt: new Date().toISOString()
    };
    heartbeats.push(heartbeat);
    
    // Keep only last 1000
    if (heartbeats.length > 1000) heartbeats = heartbeats.slice(-1000);
    
    res.json({ 
        success: true, 
        serverTime: new Date().toISOString() 
    });
});

// ============================================================
// LOCATION REQUEST (Server -> APK)
// ============================================================

// Admin endpoint to create location request (for testing)
app.post('/api/admin/konum-istek', (req, res) => {
    const request = {
        istek_id: Date.now(),
        teknisyen_id: req.body.teknisyen_id || 1,
        createdAt: new Date().toISOString()
    };
    locationRequests.push(request);
    res.json({ success: true, istek_id: request.istek_id });
});

// APK polls this endpoint
app.get('/api/konum-istek/bekleyen', authenticateToken, (req, res) => {
    const pending = locationRequests.find(r => r.teknisyen_id === req.teknisyenId);
    if (pending) {
        // Remove after delivery
        locationRequests = locationRequests.filter(r => r !== pending);
        return res.json({ istek_id: pending.istek_id });
    }
    res.json({});
});

app.post('/api/konum-gonder', authenticateToken, (req, res) => {
    const { istek_id, teknisyen_id, lat, lng, acc, t } = req.body;
    
    console.log(`Location received for request ${istek_id}: ${lat}, ${lng} (acc: ${acc}m)`);
    
    res.json({ 
        success: true, 
        receivedAt: new Date().toISOString() 
    });
});

// ============================================================
// JOBS (İŞ EMİRLERİ)
// ============================================================

app.get('/api/is-emirleri', authenticateToken, (req, res) => {
    const { atanan_id } = req.query;
    let filteredJobs = jobs;
    
    if (atanan_id) {
        filteredJobs = jobs.filter(j => j.atanan_id === parseInt(atanan_id));
    }
    
    // Return list items
    const listItems = filteredJobs.map(j => ({
        no: j.no,
        firma: j.firma,
        tarih: j.tarih,
        oncelik: j.oncelik,
        is_durumu: j.is_durumu,
        toplam_nokta: j.urunler?.length || 0
    }));
    
    res.json(listItems);
});

app.get('/api/is-emirleri/:no', authenticateToken, (req, res) => {
    const job = jobs.find(j => j.no === req.params.no);
    if (!job) {
        return res.status(404).json({ error: 'İş emri bulunamadı' });
    }
    res.json(job);
});

app.post('/api/is-emirleri/:no/checkin', authenticateToken, (req, res) => {
    const job = jobs.find(j => j.no === req.params.no);
    if (!job) {
        return res.status(404).json({ error: 'İş emri bulunamadı' });
    }
    
    job.is_durumu = 'BASLADI';
    job.checkin = req.body;
    
    res.json({ success: true, message: 'Check-in başarılı' });
});

app.post('/api/is-emirleri/:no/checkout', authenticateToken, (req, res) => {
    const job = jobs.find(j => j.no === req.params.no);
    if (!job) {
        return res.status(404).json({ error: 'İş emri bulunamadı' });
    }
    
    job.is_durumu = 'TAMAMLANDI';
    job.checkout = req.body;
    
    res.json({ success: true, message: 'Check-out başarılı' });
});

app.post('/api/is-emirleri/:no/atama', authenticateToken, (req, res) => {
    const job = jobs.find(j => j.no === req.params.no);
    if (!job) {
        return res.status(404).json({ error: 'İş emri bulunamadı' });
    }
    
    job.is_durumu = req.body.is_durumu;
    
    res.json({ success: true });
});

app.post('/api/is-emirleri/:no/foto', authenticateToken, (req, res) => {
    // Mock photo upload - in real would handle multipart
    res.json({ 
        success: true, 
        fileUrl: '/uploads/mock-photo.jpg' 
    });
});

// ============================================================
// FORMS
// ============================================================

app.post('/api/formlar', authenticateToken, (req, res) => {
    const form = {
        id: forms.length + 1,
        ...req.body,
        createdAt: new Date().toISOString()
    };
    forms.push(form);
    
    res.json({ 
        success: true, 
        form_id: form.id,
        message: 'Form başarıyla kaydedildi' 
    });
});

// ============================================================
// SYNC (Batch offline operations)
// ============================================================

app.post('/api/sync/batch', authenticateToken, (req, res) => {
    const { islemler } = req.body;
    
    const results = islemler.map((op, index) => {
        // Simulate processing
        const success = Math.random() > 0.1; // 90% success rate
        return {
            index,
            success,
            message: success ? 'İşlem başarılı' : 'İşlem başarısız (mock hata)'
        };
    });
    
    res.json({
        success: results.every(r => r.success),
        results,
        serverTime: new Date().toISOString()
    });
});

// ============================================================
// NOTIFICATIONS
// ============================================================

app.get('/api/bildirimler', authenticateToken, (req, res) => {
    res.json({ liste: notifications });
});

// ============================================================
// HEALTH CHECK
// ============================================================

app.get('/api/health', (req, res) => {
    res.json({ 
        status: 'ok', 
        timestamp: new Date().toISOString(),
        version: '1.0.0'
    });
});

// ============================================================
// STATIC FILES (for uploaded photos)
// ============================================================

app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Start server
app.listen(PORT, () => {
    console.log(`
╔══════════════════════════════════════════════════════════╗
║  SGC Teknisyen Mock Server                               ║
║  Running on http://localhost:${PORT}                        ║
║  API Base: http://localhost:${PORT}/api                    ║
╚══════════════════════════════════════════════════════════╝
    `);
});