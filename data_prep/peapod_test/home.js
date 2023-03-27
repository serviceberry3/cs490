import fetch from "node-fetch";
/*
var Peapod = require('peapod');

var config = {
    username: 'account@example.com',
    password: 'Example',
}

var peapod = new Peapod(config);

peapod.search('yogurt', function (err, results) {
    console.log(results.products);
});
*/


fetch('./index.html')
.then(function() {
    var test = document.querySelectorAll('[id^=poll-]');

    console.log(test.length)
    console.log("Done")
  })







/*
import { ApiService } from 'pdl-ui'

export default {
  get(userId, serviceLocationId, isNewProductApiServices, payload) {
    let apiPath = '/api/v4.0/user/products'
    const apiVersion = payload.apiVersion || 'v5.0'
    delete payload.apiVersion

    if (isNewProductApiServices) {
      apiPath = `/api/${apiVersion}/products/${userId}/${serviceLocationId}`
    }

    return ApiService.get(apiPath, {
      params: payload
    })
  },

  getProducts(userId, serviceLocationId, isNewProductApiServices, payload) {
    let apiPath = '/api/v4.0/user/products'

    if (isNewProductApiServices) {
      apiPath = `/api/v5.0/products/${userId}/${serviceLocationId}`
    }

    return ApiService.get(apiPath, {
      params: {
        circularId: payload.id,
        sort: 'bestMatch+asc,+name+asc',
        rows: payload.rows,
        start: payload.start,
        flags: payload.flags,
        filter: payload.filter,
        extendedInfo: payload.extendedInfo,
        nutrition: payload.nutrition,
        substitute: payload.substitute
      }
    })
  },


  submitProductRequest({
    version, description, consumerCatId, size, serviceLocationId
  }) {
    return ApiService.post(
      `/api/${version}/user/product-request`,
      {
        description, consumerCatId, size, serviceLocationId
      }
    )
  },


  getProductsById(userId, serviceLocationId, isNewProductApiServices, payload) {
    const { ids, params } = payload

    let apiPath = `/api/v4.0/user/products/${ids.join(',')}`

    if (isNewProductApiServices) {
      apiPath = `/api/v5.0/products/info/${userId}/${serviceLocationId}/${ids.join(',')}`
    }
    return ApiService.get(apiPath, { params })
  }
}*/
