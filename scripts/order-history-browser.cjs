// Optional real-browser extension of order-history-smoke.py; receives fixture session over stdin.
const { chromium } = require('../.runtime/order-browser/node_modules/playwright');
const { execFileSync } = require('node:child_process');
const assert = require('node:assert/strict');
let input = '';
process.stdin.setEncoding('utf8');
process.stdin.on('data', chunk => input += chunk);
process.stdin.on('end', async () => {
  let browser;
  try {
    const { base, orderId, foreignId, session, otherSession } = JSON.parse(input);
    assert.ok(Number.isSafeInteger(orderId) && orderId > 0);
    const statuses = {NEW:'Đơn hàng mới', CONFIRMED:'Đã xác nhận', PREPARING:'Chuẩn bị hàng', SHIPPING:'Vận chuyển', DELIVERING:'Giao hàng', DELIVERED:'Đã giao', CANCELLED:'Đơn hàng hủy', RETURNED:'Đơn hàng hoàn'};
    browser = await chromium.launch({channel:'chrome', headless:true});
    const context = await browser.newContext({viewport:{width:1440,height:1000}});
    await context.addCookies([{name:'JSESSIONID', value:session, url:base}]);
    const page = await context.newPage();
    await page.goto(base + '/home');
    await page.getByRole('link', {name:'Lịch sử đơn hàng',exact:true}).click();
    await page.screenshot({path:'.runtime/order-history-all.png',fullPage:true});
    for (const [code,label] of Object.entries(statuses)) {
      execFileSync('sqlcmd',['-S',process.env.EXAM_SQL_SERVER || '.\\SQLEXPRESS','-E','-C','-b','-l','5','-d','WebExam02_24162023','-Q',`UPDATE orders SET status='${code}' WHERE order_id=${orderId};`], {stdio:'pipe'});
      await page.reload();
      // Return to ALL if the last iteration left another filter selected.
      await page.getByRole('link',{name:'Tất cả',exact:true}).click();
      const row=page.locator(`tr[data-order-id="${orderId}"]`);
      assert.ok((await row.innerText()).includes(label));
      await page.getByRole('navigation',{name:'Lọc theo trạng thái đơn hàng'}).getByRole('link',{name:label,exact:true}).click();
      assert.equal(await row.count(),1);
      assert.equal(await page.locator('.order-filters a[aria-current="page"]').innerText(),label);
      await page.screenshot({path:`.runtime/order-history-${code}.png`,fullPage:true});
      await row.getByRole('link',{name:'Xem chi tiết'}).click();
      assert.equal(await page.locator('.order-status').innerText(),label);
      assert.ok((await page.locator('main').innerText()).includes('Original <b>snapshot</b>'));
      await page.locator('main').getByRole('link',{name:'Lịch sử đơn hàng',exact:true}).click();
      if(code !== 'NEW') {
        await page.getByRole('navigation',{name:'Lọc theo trạng thái đơn hàng'}).getByRole('link',{name:'Đơn hàng mới',exact:true}).click();
        assert.equal(await page.locator(`tr[data-order-id="${orderId}"]`).count(),0);
      }
    }
    await page.goto(base + '/orders/detail?id=' + orderId);
    await page.screenshot({path:'.runtime/order-detail.png',fullPage:true});
    assert.equal((await page.goto(base + '/orders/detail?id=' + foreignId)).status(),404);
    const other = await browser.newContext();
    await other.addCookies([{name:'JSESSIONID',value:otherSession,url:base}]);
    const otherPage=await other.newPage();
    assert.equal((await otherPage.goto(base + '/orders/detail?id=' + orderId)).status(),404);
    await page.setViewportSize({width:390,height:844});
    await page.goto(base + '/orders');
    await page.getByRole('navigation',{name:'Lọc theo trạng thái đơn hàng'}).getByRole('link',{name:'Đơn hàng hoàn',exact:true}).click();
    assert.equal(await page.locator(`tr[data-order-id="${orderId}"]`).count(),1);
    await page.screenshot({path:'.runtime/order-history-mobile.png',fullPage:true});
    console.log('PASS: Chrome trusted clicks, all eight SQL status changes, filters, details, ownership and mobile filter');
  } catch (error) {
    console.error(error.message); process.exitCode=1;
  } finally { if(browser) await browser.close(); }
});
